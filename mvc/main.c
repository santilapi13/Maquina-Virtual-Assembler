#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "parser.h"

#define MAXV 50
#define CANT_REG 16

typedef struct {
    char mnem[15];  // o rotulo
    int codOp;  // o numero de instruccion
} TReg;

typedef struct {
    char sym[10];
    int value;
} TSym;

typedef struct {
    int tipo;   // -1: ningun error ; 0: de mnemonico ; 1: de simbolo ; 2: de directivas
    int nInst;
} TErr;

void leeParametros(int argc,char *argv[],char asmFilename[],int *outputOn,char binFilename[]);
void cargaTablaMnemonicos(TReg tablaMnem[],int *NtablaMnem);
void cargaTablaRegistros(char tablaReg[][MAXV]);
int noCodeLine(char line[]);
int isInmed(char cad[]);
void procesa(char **parsed,TReg tablaMnem[],int NtablaMnem,TSym simbolos[],int *Nsym,int *nInst,TErr errores[],int *Nerr);
void wrHeader(FILE *archBin,int nInst,TErr errores[],int segmSize[]);
void decSegment(char seg[],char size[],int segmSize[]);
int errorInst(TErr errores[],int Nerr,int nInst,int tipoErr);
int isARegGral(char cad[]);
int isAReg(char cad[],char tablaReg[][MAXV]);
int decOpInd(char cad[],char tablaReg[][MAXV],TSym simbolos[],int Nsym);
int decReg(char cad[],char tablaReg[][MAXV]);
int decOpInm(char cad[],TSym simbolos[],int Nsym);
void decodifica(char **parsed,int nInst, TReg tablaMnem[],TSym simbolos[],int *cantOp,int *codOp,int *tipoOpA,int *tipoOpB,int *opA,int *opB,int NtablaMnem,int Nsym,TErr errores[],int *Nerr,char tablaReg[][50]);
void decRegGral(char codReg[],int *operando);
void trABin(int cantOp,int codOp,int tipoOpA,int tipoOpB,int opA,int opB,int *instBin,char **parsed);
void wrParsedIns(char **parsed,int nInst,TErr errores[],int Nerr,int instBin,int outputOn);
void wrBinFile(FILE *archBin,int *instBin,TErr errores[]);
void preparaParaEscritura(int *instBin);
void codUpper(char *cod,char *codOp);
int anyToInt(char *s, char **out );

int main(int argc, char *argv[]) {
    FILE *arch;
    FILE *archBin;
    char line[256],asmFilename[25],binFilename[25], tablaReg[CANT_REG][MAXV];
    TReg tablaMnem[MAXV];
    TSym simbolos[MAXV];
    char **parsed;
    int nInst,instBin, cantOp, codOp, tipoOpA, tipoOpB, opA, opB,NtablaMnem,Nsym=0,Nerr=0,outputOn=1,segmSize[3];
    TErr errores[MAXV];

    leeParametros(argc,argv,asmFilename,&outputOn,binFilename); // Parametros pasados por consola
    cargaTablaMnemonicos(tablaMnem,&NtablaMnem);   // Crea tabla con codigos de operacion y mnemonico
    cargaTablaRegistros(tablaReg);
    segmSize[0] = segmSize[1] = segmSize[2] = 1024; // 0: data ; 1: extra ; 2: stack ; 3: code

    if ((arch=fopen(asmFilename,"r")) != NULL) {    // PRIMERA PASADA
        nInst = 0;
        errores[0].tipo = -1;
        while (fgets(line,256,arch) != NULL) {
            if (!noCodeLine(line)) {
                parsed = parseline(line);
                if (parsed[5] != NULL && parsed[6] != NULL)    // verifica que no sea una directiva
                    decSegment(parsed[5],parsed[6],segmSize);
                else
                    procesa(parsed,tablaMnem,NtablaMnem,simbolos,&Nsym,&nInst,errores,&Nerr);    // Guarda simbolos y busca errores
            }
        }
        freeline(parsed);
        fclose(arch);
    }

    for (int i=0;i<Nsym;i++)
        printf("SIMBOLO %d: %s %d\n",i,simbolos[i].sym,simbolos[i].value);

    if ((arch=fopen(asmFilename,"r")) != NULL) {    // SEGUNDA PASADA
        archBin = fopen(binFilename,"wb");
        wrHeader(archBin,nInst,errores,segmSize);   // Escribe el header en .mv1
        nInst = 0;
        while (fgets(line,256,arch) != NULL) {
            if (!noCodeLine(line)) {
                parsed = parseline(line);
                if (parsed[5]==NULL && parsed[6]==NULL && parsed[7]==NULL && parsed[8]==NULL) {
                    if (!errorInst(errores,Nerr,nInst,0))    // Si la instruccion actual no tiene un error de mnemonico no debe decodificar
                        decodifica(parsed,nInst,tablaMnem,simbolos,&cantOp,&codOp,&tipoOpA,&tipoOpB,&opA,&opB,NtablaMnem,Nsym,errores,&Nerr,tablaReg);
                    trABin(cantOp,codOp,tipoOpA,tipoOpB,opA,opB,&instBin,parsed);
                    wrParsedIns(parsed,nInst,errores,Nerr,instBin,outputOn);   // Imprime por pantalla
                    wrBinFile(archBin,&instBin,errores);    // Escribe .mv1
                    nInst++;
                } else
                    printf("%s",line);
            } else
                printf("%s",line);
        }
        freeline(parsed);
        fclose(arch);
        fclose(archBin);
        if (errores[0].tipo != -1) {
            printf("\nLa traduccion no tuvo exito por la presencia de 1 o mas errores.\n");
            remove(binFilename);
        }
        else
            printf("\nTraduccion exitosa.\n");
    }
    return 0;
}

void leeParametros(int argc,char *argv[],char asmFilename[],int *outputOn,char binFilename[]) {
    unsigned int i;
    char *extFile;
    for (i=1;i<argc;i++)
        if (!strcmp(argv[i],"-o"))
            *outputOn = 0;
        else {
            extFile = argv[i] + strlen(argv[i]) - 4;
            if (!strcmp(extFile,".asm"))
                strcpy(asmFilename,argv[i]);
            else if (!strcmp(extFile,".mv1"))
                strcpy(binFilename,argv[i]);
        }
}

void cargaTablaMnemonicos(TReg tablaMnem[],int *NtablaMnem) {

    strcpy(tablaMnem[0].mnem,"MOV");
    tablaMnem[0].codOp = 0;
    strcpy(tablaMnem[1].mnem,"ADD");
    tablaMnem[1].codOp = 1;
    strcpy(tablaMnem[2].mnem,"SUB");
    tablaMnem[2].codOp = 2;
    strcpy(tablaMnem[3].mnem,"SWAP");
    tablaMnem[3].codOp = 3;
    strcpy(tablaMnem[4].mnem,"MUL");
    tablaMnem[4].codOp = 4;
    strcpy(tablaMnem[5].mnem,"DIV");
    tablaMnem[5].codOp = 5;
    strcpy(tablaMnem[6].mnem,"CMP");
    tablaMnem[6].codOp = 6;
    strcpy(tablaMnem[7].mnem,"SHL");
    tablaMnem[7].codOp = 7;
    strcpy(tablaMnem[8].mnem,"SHR");
    tablaMnem[8].codOp = 8;
    strcpy(tablaMnem[9].mnem,"AND");
    tablaMnem[9].codOp = 9;
    strcpy(tablaMnem[10].mnem,"OR");
    tablaMnem[10].codOp = 10;
    strcpy(tablaMnem[11].mnem,"XOR");
    tablaMnem[11].codOp = 11;

    strcpy(tablaMnem[12].mnem,"SYS");
    tablaMnem[12].codOp = 240;
    strcpy(tablaMnem[13].mnem,"JMP");
    tablaMnem[13].codOp = 241;
    strcpy(tablaMnem[14].mnem,"JZ");
    tablaMnem[14].codOp = 242;
    strcpy(tablaMnem[15].mnem,"JP");
    tablaMnem[15].codOp = 243;
    strcpy(tablaMnem[16].mnem,"JN");
    tablaMnem[16].codOp = 244;
    strcpy(tablaMnem[17].mnem,"JNZ");
    tablaMnem[17].codOp = 245;
    strcpy(tablaMnem[18].mnem,"JNP");
    tablaMnem[18].codOp = 246;
    strcpy(tablaMnem[19].mnem,"JNN");
    tablaMnem[19].codOp = 247;
    strcpy(tablaMnem[20].mnem,"LDL");
    tablaMnem[20].codOp = 248;
    strcpy(tablaMnem[21].mnem,"LDH");
    tablaMnem[21].codOp = 249;
    strcpy(tablaMnem[22].mnem,"RND");
    tablaMnem[22].codOp = 250;
    strcpy(tablaMnem[23].mnem,"NOT");
    tablaMnem[23].codOp = 251;

    strcpy(tablaMnem[24].mnem,"STOP");
    tablaMnem[24].codOp = 4081;
    *NtablaMnem = 25;

    strcpy(tablaMnem[25].mnem,"SLEN");
    tablaMnem[25].codOp = 0xC;
    strcpy(tablaMnem[26].mnem,"SMOV");
    tablaMnem[26].codOp = 0xD;
    strcpy(tablaMnem[27].mnem,"SCMP");
    tablaMnem[27].codOp = 0xE;

    strcpy(tablaMnem[28].mnem,"PUSH");
    tablaMnem[28].codOp = 0xFC;
    strcpy(tablaMnem[29].mnem,"POP");
    tablaMnem[29].codOp = 0xFD;
    strcpy(tablaMnem[30].mnem,"CALL");
    tablaMnem[30].codOp = 0xFE;
    strcpy(tablaMnem[31].mnem,"RET");
    tablaMnem[31].codOp = 0xFF0;
}

void cargaTablaRegistros(char tablaReg[][MAXV]) {
    strcpy(tablaReg[0],"DS");
    strcpy(tablaReg[1],"SS");
    strcpy(tablaReg[2],"ES");
    strcpy(tablaReg[3],"CS");
    strcpy(tablaReg[4],"HP");
    strcpy(tablaReg[5],"IP");
    strcpy(tablaReg[6],"SP");
    strcpy(tablaReg[7],"BP");
    strcpy(tablaReg[8],"CC");
    strcpy(tablaReg[9],"AC");
    strcpy(tablaReg[10],"EAX");
    strcpy(tablaReg[11],"EBX");
    strcpy(tablaReg[12],"ECX");
    strcpy(tablaReg[13],"EDX");
    strcpy(tablaReg[14],"EEX");
    strcpy(tablaReg[15],"EFX");
}

int noCodeLine(char line[]) {
    int i=0;
    while (i<strlen(line) && (line[i]==' ' || line[i]=='\t'))
        i++;
    return !(strcmp(line,"\n") && i<strlen(line) && line[i] != ';');
}

int isInmed(char cad[]) {
    return (cad[0]>='0' && cad[0]<= '9') || cad[0]=='-' || cad[0]=='%' || cad[0]=='#' || cad[0]=='@' || cad[0] == 39;
}

void procesa(char **parsed,TReg tablaMnem[],int Ntabla,TSym simbolos[],int *Nsym,int *nInst,TErr errores[],int *Nerr) {
    char mnem[6],simbolo[10],*out;
    int i;

    // DECODIFICA LABEL Y GUARDA SU CORRESPONDIENTE NRO DE INSTRUCCION
    if (parsed[0]) {    // Si tiene rótulo
        codUpper(parsed[0],simbolo);
        i=0;
        while (i<*Nsym && strcmp(simbolo,simbolos[i].sym))
            i++;
        if (i >= *Nsym) {
            strcpy(simbolos[*Nsym].sym,simbolo);
            simbolos[(*Nsym)++].value = *nInst;
        } else {    // Simbolo duplicado
            printf("ERROR: Simbolo %s duplicado en linea %d\n",parsed[0],*nInst);
            errores[*Nerr].tipo = 1;
            errores[(*Nerr)++].nInst = *nInst;
        }
    }

    // DECODIFICA EQU Y GUARDA SU VALOR (O DIRECCION DEL CS SI ES STRING)
    if (parsed[7]) {    // si tiene EQU
        codUpper(parsed[7],simbolo);
        i=0;
        while (i<*Nsym && strcmp(simbolo,simbolos[i].sym))
            i++;
        if (i >= *Nsym) {
            strcpy(simbolos[*Nsym].sym,simbolo);
            if (isInmed(parsed[8]))
                simbolos[(*Nsym)++].value = anyToInt(parsed[8],&out);  // VER COMO HACER CON LOS EQU STRINGS
            else {

            }
        } else {    // Simbolo duplicado
            printf("ERROR: Simbolo %s duplicado en linea %d\n",parsed[7],*nInst);
            errores[*Nerr].tipo = 1;
            errores[(*Nerr)++].nInst = *nInst;
        }
    }

    // DECODIFICA MNEMONICO EN BUSCA DE ERROR
    if (parsed[1]) {    // Si es una instruccion
        codUpper(parsed[1],mnem);
        i = 0;
        while (i<Ntabla && strcmp(mnem,tablaMnem[i].mnem))    // Busca si hay error por mnemonico inexistente
            i++;
        if (i>=Ntabla) {  // Guarda el nro de instruccion en el que hay error
            printf("ERROR: Mnemonico %s inexistente o mal escrito en linea %d\n",parsed[1],*nInst);
            errores[*Nerr].tipo = 0;
            errores[(*Nerr)++].nInst = *nInst;
        }
        (*nInst)++;
    }
}

void wrHeader(FILE *archBin,int nInst,TErr errores[],int segmSize[]) {
    int segmAct;
    if (errores[0].tipo == -1) {
        fwrite("MV-2",4,1,archBin);    // 4 chars fijos

        segmAct = segmSize[0];
        preparaParaEscritura(&segmAct);
        fwrite(&segmAct,4,1,archBin); // Tamaño del DS
        segmAct = segmSize[2];
        preparaParaEscritura(&segmAct);
        fwrite(&segmAct,4,1,archBin);  // Tamaño del SS
        segmAct = segmSize[1];
        preparaParaEscritura(&segmAct);
        fwrite(&segmAct,4,1,archBin);  // Tamaño del ES
        //fwrite(&cero,4,1,archBin);  // Tamaño del CS

        fwrite("V.22",4,1,archBin);    // 4 chars fijos
    }
}

void decSegment(char seg[],char size[],int segmSize[]) {
    char segment[5],aux = *size;
    int tam,i=1;
    while(aux != '\0') {    // pasa a int
        tam = tam*10 + aux-48;
        aux = *(size+i);
        i++;
    }
    codUpper(seg,segment);
    if (tam > 0 && tam < 0xFFFF) {
        if (!strcmp(segment,"DATA"))
            if (segmSize[0] == 1024)
                segmSize[0] = tam;
            else
                printf("WARNING: Directiva referente a DATA SEGMENT repetida. Se toma valor de la primera aparicion.\n");
        else if(!strcmp(segment,"EXTRA"))
            if (segmSize[1] == 1024)
                segmSize[1] = tam;
            else
                printf("WARNING: Directiva referente a EXTRA SEGMENT repetida. Se toma valor de la primera aparicion.\n");
        else if (!strcmp(segment,"STACK"))
            if (segmSize[2] == 1024)
                segmSize[2] = tam;
            else
                printf("WARNING: Directiva referente a STACK SEGMENT repetida. Se toma valor de la primera aparicion.\n");
        else
            printf("WARNING: Nombre de segmento inexistente. Linea ignorada.\n");
    } else
        printf("WARNING: Valor de segmento invalido. Linea ignorada.");
}

int errorInst(TErr errores[],int Nerr,int nInst,int tipoErr) {
    int i=0;
    while (i < Nerr && errores[i].nInst != nInst)
        i++;
    return i < Nerr && errores[i].nInst == nInst && errores[i].tipo == tipoErr;
}

int isARegGral(char cad[]) {
    int isReg=0;

    if (strlen(cad) == 2) {
        if (cad[0] >= 'A' && cad[0] <= 'F' && (cad[1] == 'X' || cad[1] == 'H' || cad[1] == 'L') )
            isReg = 1;
    } else if (strlen(cad) == 3) {
        if (cad[0] == 'E' && cad[1] >= 'A' && cad[1] <= 'F' && cad[2] == 'X')
            isReg = 1;
    }

    return isReg;
}

int isAReg(char cad[],char tablaReg[][MAXV]) {
    int i=0;
    char aux[10];
    codUpper(cad,aux);

    while (i<CANT_REG-5 && strcmp(aux,tablaReg[i]))
        i++;

    return !strcmp(aux,tablaReg[i]) || isARegGral(aux);
}

int decCodOp(char cad[],TReg tablaMnem[],int NtablaMnem) {
    int i = 0;
    char mnem[6];
    codUpper(cad,mnem);
    while (i<NtablaMnem && strcmp(mnem,tablaMnem[i].mnem))    // Ya se verificó que no halla error
        i++;
    return tablaMnem[i].codOp;
}

int decOpInd(char cad[],char tablaReg[][MAXV],TSym simbolos[],int Nsym) {
    int offset=0,i=0;
    while (cad[i] != ']' && (cad[i] != '+' || cad[i] != '-'))
        i++;
    if (cad[i] == '+' || cad[i] == '-')
        offset = decOpInm(cad+i+1,simbolos,Nsym);
    return ((offset << 4) & 0xFF0) | (decReg(cad,tablaReg) & 0xF);
}

int decReg(char cad[],char tablaReg[][MAXV]) {
    char codReg[10];
    int i=0,aux;
    codUpper(cad,codReg);
    while (i < 10 && strcmp(codReg,tablaReg[i]))
        i++;
    if (i < 10 && !strcmp(codReg,tablaReg[i]))
        aux = i;
    else      // Es un General Purpose Register (decodificacion especial)
        decRegGral(codReg,&aux);
    return aux;
}

int decOpInm(char cad[],TSym simbolos[],int Nsym) {
    char *out,op[10];
    int aux,i=0;
    if (isInmed(cad))
        aux = anyToInt(cad,&out);
    else {
        codUpper(cad,op);
        while (i < Nsym && strcmp(op,simbolos[i].sym))
            i++;
        if (i < Nsym && !strcmp(op,simbolos[i].sym))
            aux = simbolos[i].value;
        else
            aux = 0x7FFFFFFF;
    }
    return aux;
}

void decodifica(char **parsed,int nInst, TReg tablaMnem[],TSym simbolos[],int *cantOp,int *codOp,int *tipoOpA,int *tipoOpB,int *opA,int *opB,int NtablaMnem,int Nsym,TErr errores[],int *Nerr,char tablaReg[][50]) {
    char *out;
    // DECODIFICA CANTIDAD DE OPERANDOS
    if (!parsed[2])
        *cantOp = 0;
    else if (!parsed[3])
        *cantOp = 1;
    else
        *cantOp = 2;

    // DECODIFICA CODIGO DE OPERACION EN DECIMAL
    *codOp = decCodOp(parsed[1],tablaMnem,NtablaMnem);

    // DECODIFICA TIPO DE OPERANDO A Y/O B SEGUN CORRESPONDA EN DECIMAL (0:inmediato - 1:registro - 2:directo - 3:indirecto)
    if (*cantOp != 0) {
        if (isAReg(parsed[2],tablaReg))
            *tipoOpA = 1;           // A de registro
        else if (parsed[2][0] == '[') {
            if (isInmed(parsed[2]+1))
                *tipoOpA = 2;       // A directo
            else
                *tipoOpA = 3;       // A indirecto
        } else
            *tipoOpA = 0;           // A inmediato

        if (*cantOp == 2){
            if (isAReg(parsed[3],tablaReg))
                *tipoOpB = 1;       // B de registro
            else if (parsed[3][0] == '[') {
                if (isInmed(parsed[3]+1))
                    *tipoOpB = 2;   // B directo
                else
                    *tipoOpB = 3;   // B indirecto
            } else
                *tipoOpB = 0;       // B inmediato
        }
    }

    // DECODIFICA OPERANDO A Y/O B SEGUN CORRESPONDA EN DECIMAL
    if (*cantOp != 0) {     // Decodifica operando A
        if (*tipoOpA == 1)            // tipo REGISTRO
            *opA = decReg(parsed[2],tablaReg);
        else if (*tipoOpA == 3)       // tipo INDIRECTO
            *opA = decOpInd(parsed[2]+1,tablaReg,simbolos,Nsym);
        else if (*tipoOpA == 2)       // tipo DIRECTO
            if (parsed[2][1] == 39)
                *opA = parsed[2][2];
            else
                *opA = anyToInt(parsed[2]+1,&out);
        else                          // tipo INMEDIATO
            if (parsed[2][0] == 39)
                *opA = parsed[2][1];
            else {
                *opA = decOpInm(parsed[2],simbolos,Nsym);
                if (*opA == 0x7FFFFFFF) {
                    printf("ERROR: Simbolo %s inexistente\n",parsed[2]);
                    errores[*Nerr].tipo = 1;
                    errores[(*Nerr)++].nInst = nInst;
                    *opA = (*cantOp == 2) ? (0xFFF) : (0xFFFF);
                }
            }
    }

    if (*cantOp == 2) {     // Si tiene operando B hace exactamente lo mismo
        if (*tipoOpB == 1)             // tipo REGISTRO
            *opB = decReg(parsed[3],tablaReg);
        else if (*tipoOpB == 3)        // tipo INDIRECTO
            *opB = decOpInd(parsed[3],tablaReg,simbolos,Nsym);
        else if (*tipoOpB == 2)       // tipo DIRECTO
            if (parsed[3][1] == 39)
                *opB = parsed[3][2];
            else
                *opB = anyToInt(parsed[3]+1,&out);
        else                          // tipo INMEDIATO
            if (parsed[3][0] == 39)
                *opB = parsed[3][1];
            else {
                *opB = decOpInm(parsed[3],simbolos,Nsym);
                if (*opB == 0x7FFFFFFF) {
                    printf("ERROR: Simbolo %s inexistente\n",parsed[3]);
                    errores[*Nerr].tipo = 1;
                    errores[(*Nerr)++].nInst = nInst;
                    *opB = 0xFFF;
                }
            }
    }
}

void decRegGral(char codReg[],int *operando) {
    if (strlen(codReg) == 3)     //  4 bytes (porque son 3 caracteres)
        *operando = codReg[1] - 55;   // Resta 55 porque es la forma de pasar el caracter ASCII (hexa) a decimal
    else
        if (codReg[1] == 'X')    //  2 ultimos bytes (porque el 2do caracter es X)
            *operando = codReg[0] - 55 + 48;
        else if (codReg[1] == 'L')   // 4to byte (porque el 2do caracter es L)
            *operando = codReg[0] - 55 + 16;
        else                            // 3er byte (porque el 2do caracter es H)
        *operando = codReg[0] - 55 + 32;
}

void trABin(int cantOp,int codOp,int tipoOpA,int tipoOpB,int opA,int opB,int *instBin,char **parsed) {
    *instBin = 0;   // 32 bits en 0
    if (cantOp == 0)
        *instBin = codOp << 20;
    else if (cantOp == 1) {
        *instBin = (codOp << 24) | ((tipoOpA << 22) & 0x00C00000) | (opA & 0x0000FFFF);
        if (tipoOpA == 0 && (opA<-65536 || opA>65535))
            printf("WARNING: Inmediato truncado: %s\n", parsed[2]);
    } else {
        *instBin = (codOp << 28) | ((tipoOpA << 26) & 0x0C000000) | ((tipoOpB << 24) & 0x03000000) | ((opA << 12) & 0x00FFF000) | (opB & 0x00000FFF);
        if (tipoOpB == 0 && (opB<-4096 || opB>4095))
            printf("WARNING: Inmediato truncado: %s\n", parsed[3]);
        if (tipoOpA == 0 && (opA<-4096 || opA>4095))
            printf("WARNING: Inmediato truncado: %s\n", parsed[2]);
    }
}

int anyToInt(char *s, char **out ) {
    char *BASES = {"**$*****@*#*****%"};
    int base = 10;
    char *bp = strchr(BASES,*s);
    if (bp != NULL) {
        base = bp - BASES;
        ++s;
    }
    return strtol(s,out,base);
}

void codUpper(char *cod,char codOp[]) { // Pasa string a mayuscula
    unsigned int i=0;
    while ((cod[i]>= 'a' && cod[i]<='z') || (cod[i]>= 'A' && cod[i]<='Z') || (cod[i]>= '0' && cod[i]<'9')) {
        codOp[i] = (cod[i]>='a' && cod[i]<='z') ? (cod[i]-32) : (cod[i]);
        i++;
    }
    codOp[i] = '\0';
}

void wrParsedIns(char **parsed,int nInst,TErr errores[],int Nerr,int instBin,int outputOn) {
    if (outputOn) {
        if (nInst < 10)
            printf("[000%d]: ",nInst);
        else if (nInst < 100)
            printf("[00%d]: ",nInst);
        else if (nInst < 1000)
            printf("[0%d]: ",nInst);
        else if (nInst < 10000)
            printf("[%d]: ",nInst);

        if (errorInst(errores,Nerr,nInst,0))   // Error por mnemónico inexistente
            printf("FF FF FF FF\t");
        else
            printf("%02X %02X %02X %02X\t",(instBin>>24)&0xFF,(instBin>>16)&0xFF,(instBin>>8)&0xFF,instBin&0xFF);

        if (parsed[0])
            printf("%9s: ",parsed[0]);
        else
            printf("%9d: ",nInst+1);
        printf("%s\t%7s",parsed[1],parsed[2] ? parsed[2]:"");
        if (parsed[3])
            printf(", %s",parsed[3]);
        else
            printf("\t");
        if (parsed[4])
            printf("\t; %s",parsed[4]);
        printf("\n");
    }
}

void wrBinFile(FILE *archBin,int *instBin,TErr errores[]) {
    if (errores[0].tipo == -1) {   // si hay al menos une error no escribe
        preparaParaEscritura(instBin);// Pasa de littleEndian a bigEndian
        fwrite(instBin,4,1,archBin);      // Escribe arch binario (si hubo error no)
    }
}

void preparaParaEscritura(int *instBin) {
    unsigned int b0,b1,b2,b3;
    b0 = ((*instBin) & 0x000000FF) <<24;
    b1 = ((*instBin) & 0x0000FF00) <<8;
    b2 = ((*instBin) & 0x00FF0000) >>8;
    b3 = ((*instBin) & 0xFF000000) >>24;
    *instBin = b0 | b1 | b2 | b3;
}
