package paquete;

import java.io.IOException;
import java.util.Scanner;

public class ALU {
	
	private static boolean parametrob = false;
	private static boolean parametroc = false;
	private static boolean parametrod = false;
	
	private final int mask0 = 0xFFFFFFFF;
	private final int maskf = 0x0000000F;
	
	

	public static boolean getParametrob() {
		return parametrob;
	}

	public static boolean getParametroc() {
		return parametroc;
	}

	public static boolean getParametrod() {
		return parametrod;
	}

	public void setParametroB(boolean paramB) {
        parametrob=paramB;
    }
	
    public void setParametroC(boolean paramC) {
        parametroc=paramC;
    }
    
    public void setParametroD(boolean paramD) {
        parametrod=paramD;
    }
		
	public void mov(int topA, int topB, int vopA, int vopB){ //parametros en decimal

		int b = valor2(topB,vopB), copia = vopA, accesoA = copia>>4;
		int maskAccRegistro = accesoRegistro(accesoA);
		int nuevovalor,numreg;
		if (topA == 1) { //Registro
			numreg=vopA & maskf;		
			if (accesoA == 2) { //modifico valor registro (caso 3byte)		
				nuevovalor=(Registros.getInstancia().getReg(numreg)&~maskAccRegistro)| (b<<8 & maskAccRegistro);		
			}
			else 
				nuevovalor=((Registros.getInstancia().getReg(vopA & maskf)& ~maskAccRegistro))| (b & maskAccRegistro);
			Registros.getInstancia().modificaReg(numreg, nuevovalor);
		}
		else{
			if (topA == 2)          // Directo 
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(),b);
			else
				if (topA == 3) 					//Indirecto
					Memoria.getInstancia().modificaRAM(validarSegmento(vopA),b);
		}
	}

	public void add(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA, accesoA = copia>>4, maskAccRegistro = accesoRegistro(accesoA),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf)| ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
			if (accesoA == 2) b = b<<8;
			resultado = valor + b;
			Registros.getInstancia().modificaReg(vopA & maskf,(((Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | ((resultado) & maskAccRegistro)) & mask0));
		}
		else
			if (topA == 3) {					//Indirecto
				resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) + b;
				Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
			}
			else{
				resultado = Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()) + b;
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(),resultado & mask0);
			}
		this.seteaCC(resultado);
	}
	
	public void sub(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA,accesoA = copia>>4, maskAccRegistro = accesoRegistro(accesoA),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;	
			if (accesoA == 2) b = b<<8;
			resultado = (valor&maskAccRegistro)  - (b&maskAccRegistro);
			Registros.getInstancia().modificaReg(vopA & maskf,(Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | (resultado & maskAccRegistro));
		}
		else
			if (topA == 3) {					//Indirecto
				resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) - b;
				Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
			}
			else{
				resultado = Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow())- b;
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(), resultado & mask0);
			}
		this.seteaCC(resultado);
	}
	
	public void mul(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA,accesoA = copia>>4, maskAccRegistro = accesoRegistro(accesoA),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
			if (accesoA == 2) b = b<<8;
			resultado = valor * b;
			Registros.getInstancia().modificaReg(vopA & maskf, ((Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | ((resultado) & maskAccRegistro)) & mask0);

		}
		else
			if (topA == 3) {					//Indirecto
				resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) * b;
				Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
			}
			else{
				resultado = Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()) * b;
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(), resultado & mask0);
			}
		this.seteaCC(resultado);
	}
	
	public void div(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA,accesoA = copia>>4, maskAccRegistro = accesoRegistro(accesoA),resultado;
		if (b!=0) {	
			if (topA == 1) {  	//Registro
				if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
					valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
				else 
					valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
				if (accesoA == 2) b = b<<8;
				Registros.getInstancia().setRegistro("AC",valor % b);
				resultado = valor / b;
				Registros.getInstancia().modificaReg(vopA & maskf, ((Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | ((resultado) & maskAccRegistro)) & mask0);
			}
			else
				if (topA == 3) {					//Indirecto
					Registros.getInstancia().setRegistro("AC",Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) % b);
					resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) / b;
					Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
				}
				else{
					Registros.getInstancia().setRegistro("AC",Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()) % b);
					resultado = Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()) / b;
					Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(), resultado & mask0);
				}
			this.seteaCC(resultado);			
		}
	}
	

	public void swap(int topA, int topB, int vopA, int vopB){
		int copiaA = vopA,copiaB = vopB,accesoA = copiaA>>4, maskAccRegistroA = accesoRegistro(accesoA),aux,accesoB = copiaB>>4,maskAccRegistroB = accesoRegistro(accesoB);
		if (topA == 1) {  	//Registro
				if (topB == 1){
					aux= (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistroA);
					if (accesoB == 2)
						Registros.getInstancia().modificaReg(vopA & maskf, ((Registros.getInstancia().getReg(vopB & maskf) & maskAccRegistroB)>>8 & maskAccRegistroA));
					else {
						Registros.getInstancia().modificaReg(vopA & maskf, (Registros.getInstancia().getReg(vopB & maskf) & maskAccRegistroB) & maskAccRegistroA);
						Registros.getInstancia().modificaReg(vopB & maskf, aux & maskAccRegistroB);
					}
				}
				else
					if (topB == 3) {					//Indirecto
						aux = Memoria.getInstancia().getValorRAM(validarSegmento(vopB));
						Memoria.getInstancia().modificaRAM(validarSegmento(vopB),Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistroA);
						Registros.getInstancia().modificaReg(vopA & maskf, aux);
					}
					else {
						aux= Memoria.getInstancia().getValorRAM(vopB + Registros.getInstancia().getDSLow());
						Memoria.getInstancia().modificaRAM(vopB + Registros.getInstancia().getDSLow(), Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistroA);
						Registros.getInstancia().modificaReg(vopA & maskf, aux);
					}
			}
			else
				if (topA == 2){
					if (topB == 1){	
						aux=Registros.getInstancia().getReg(vopB & maskf) & maskAccRegistroB;
						Registros.getInstancia().modificaReg(vopB & maskf, Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()));
						Memoria.getInstancia().modificaRAM( vopA + Registros.getInstancia().getDSLow(),aux);
					}
					else {
						if (topB == 3) {					//Indirecto
							aux = Memoria.getInstancia().getValorRAM(validarSegmento(vopB));
							Memoria.getInstancia().modificaRAM(validarSegmento(vopB),Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()));
							Memoria.getInstancia().modificaRAM(Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()),aux);
						}
						else {
							aux=Memoria.getInstancia().getValorRAM(vopB + Registros.getInstancia().getDSLow());
							Memoria.getInstancia().modificaRAM(vopB+ Registros.getInstancia().getDSLow(), Memoria.getInstancia().getValorRAM(vopA+ Registros.getInstancia().getDSLow())); 
							Memoria.getInstancia().modificaRAM(vopA+ Registros.getInstancia().getDSLow(), aux);
		
						}
					}
				}
				else {
					if (topB == 1){	
						aux=Registros.getInstancia().getReg(vopB & maskf) & maskAccRegistroB;
						Registros.getInstancia().modificaReg(vopB & maskf, Memoria.getInstancia().getValorRAM(validarSegmento(vopA)));
						Memoria.getInstancia().modificaRAM( validarSegmento(vopA),aux);
					}
					else {
						if (topB == 3) {					//Indirecto
							aux = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
							Memoria.getInstancia().modificaRAM(validarSegmento(vopA),Memoria.getInstancia().getValorRAM(validarSegmento(vopB)));
							Memoria.getInstancia().modificaRAM(validarSegmento(vopB),aux);
						}
						else {
							aux=Memoria.getInstancia().getValorRAM(vopB + Registros.getInstancia().getDSLow());
							Memoria.getInstancia().modificaRAM(vopB+ Registros.getInstancia().getDSLow(), Memoria.getInstancia().getValorRAM(validarSegmento(vopA))); 
							Memoria.getInstancia().modificaRAM(validarSegmento(vopA), aux);
		
						}
					}
		
				}
	}
	
	public void cmp(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA, maskAccRegistro = accesoRegistro(copia>>4),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
			resultado = valor - b;
		}
		else
			if (topA == 2)	// directo
				resultado = Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()) - b;
			else
				if (topA == 3) 					//Indirecto
					resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) - b;
				else
					resultado = vopA - b;
		this.seteaCC(resultado);
	}
	
	public void and(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA, maskAccRegistro = accesoRegistro(copia>>4),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
			resultado = valor & b;
			Registros.getInstancia().modificaReg(vopA & maskf, ((Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | ((resultado) & maskAccRegistro)) & mask0);
		}
		else
			if (topA == 3) {					//Indirecto
				resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) & b;
				Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
			}
			else{
				resultado =Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()) & b;
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDS(), resultado & mask0);
			}
		this.seteaCC(resultado);
	}
	
	public void or(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA, maskAccRegistro = accesoRegistro(copia>>4),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
			resultado = valor | b;
			Registros.getInstancia().modificaReg(vopA & maskf, ((Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | ((resultado) & maskAccRegistro)) & mask0);
		}
		else
			if (topA == 3) {					//Indirecto
				resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) | b;
				Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
			}
			else{
				resultado=Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow())|b;
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(), resultado & mask0);
			}
		this.seteaCC(resultado);
	}
	
	public void xor(int topA, int topB, int vopA, int vopB){
		int b = valor2(vopB,vopB),valor, copia = vopA, maskAccRegistro = accesoRegistro(copia>>4),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
			resultado = valor ^ b;
			Registros.getInstancia().modificaReg(vopA & maskf, ((Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | ((resultado) & maskAccRegistro)) & mask0);
		}
		else
			if (topA == 3) {					//Indirecto
				resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) ^ b;
				Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
			}
			else{
				resultado = Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow())^ b;
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(), resultado & mask0);
			}
		this.seteaCC(resultado);
	}
	
	public void shl(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA, maskAccRegistro = accesoRegistro(copia>>4),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
			resultado = valor << b;
			Registros.getInstancia().modificaReg(vopA & maskf, ((Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | ((resultado) & maskAccRegistro)) & mask0);
		}
		else
			if (topA == 3) {					//Indirecto
				resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) << b ;
				Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
			}
			else{
				resultado = Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow())<< b;
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(), resultado & mask0);
			}
		this.seteaCC(resultado);
	}
	
	public void shr(int topA, int topB, int vopA, int vopB){
		int b = valor2(topB,vopB),valor, copia = vopA, maskAccRegistro = accesoRegistro(copia>>4),resultado;
		if (topA == 1) {  	//Registro
			if (esNegativo(Registros.getInstancia().getReg(vopA & maskf),maskAccRegistro)) 
				valor = Registros.getInstancia().getReg(vopA & maskf) | ~maskAccRegistro;
			else 
				valor = Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro;
			resultado = valor >> b;
			Registros.getInstancia().modificaReg(vopA & maskf, ((Registros.getInstancia().getReg(vopA & maskf) & ~maskAccRegistro) | ((resultado) & maskAccRegistro) & mask0));
		}
		else
			if (topA == 3) {					//Indirecto
				resultado = Memoria.getInstancia().getValorRAM(validarSegmento(vopA)) >> b;
				Memoria.getInstancia().modificaRAM(validarSegmento(vopA),resultado & mask0);
			}
			else{
				resultado=Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDSLow()) >> b;
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(), resultado & mask0);
			}
		this.seteaCC(resultado);
	}
	
	public void sys (int vopA) throws InterruptedException, IOException {
		switch (vopA & 0xF) {
			case 1: lectura();break;
			case 2: escritura();break;
			case 3: lecturaString();break;
			case 4: escrituraString();break;
			case 7: clearScreen();break;
			case 15: breakpoints(vopA); break;
		}
	}
	
public void lectura(){
String aux;
boolean prompt = true, saltoLinea = true;
int a,comienzo, cantidad,cont,auxInt;
comienzo = Registros.getInstancia().getEDX();
cantidad = Registros.getInstancia().getCX();
Scanner leer = new Scanner(System.in);
	if (Registros.getInstancia().getAX()>>11 == 1) 
		prompt = false;
	if (((Registros.getInstancia().getAX()>>8) & 0X00000003) == 1) 
		saltoLinea = false;	
	if (!saltoLinea) {
		if (prompt)
			System.out.format("[%04d]",comienzo);
		aux = leer.nextLine();
		cont = 0;
		for (a = comienzo;a<(comienzo + cantidad);a++) {    //pasar octal o hexa
			Memoria.getInstancia().modificaRAM(a+Registros.getInstancia().getDSLow(),aux.charAt(cont));
			cont++;
		}
	}	
	else {
		for (a = comienzo;a<(comienzo+cantidad);a++) {		
			if (prompt)
				System.out.format("[%04d]",comienzo);
				auxInt = leer.nextInt();
				Memoria.getInstancia().modificaRAM(comienzo+Registros.getInstancia().getDSLow(), auxInt);		
		}
	}
	leer.close();
}
	
public void escritura(){
	boolean prompt = true, saltoLinea = true;
	int a,comienzo, cantidad,salida;
	comienzo = Registros.getInstancia().getEDX();
	cantidad = Registros.getInstancia().getCX();
		if ((Registros.getInstancia().getAX() & 0x00000800) == 0x00000800)
			prompt = false;
		if (((Registros.getInstancia().getAX()>>8) & 0X00000100) == 0X00000100)
			saltoLinea = false;
		for (a = comienzo;a<(comienzo+cantidad);a++) {
			if (prompt)
				System.out.format("[%04d] ",a);		
			if (((Registros.getInstancia().getAX() & 0x0000010) ==  0X00000010)){
				salida = (char)Memoria.getInstancia().getValorRAM(a +Registros.getInstancia().getDSLow())& 0X000000FF;
				if (((salida>=0) && (salida<=31)) || (salida ==127)) 
					salida =(char)46;
				else
					salida=(char)Memoria.getInstancia().getValorRAM(a + Registros.getInstancia().getDSLow());
				System.out.format("'%c ",salida);
			}
			salida=Memoria.getInstancia().getValorRAM(a + Registros.getInstancia().getDSLow());
			if (((Registros.getInstancia().getAX()& 0x0000008) ==  0X00000008))
				System.out.format("H%X ",salida);
			if (((Registros.getInstancia().getAX()& 0x0000004) ==  0X00000004))  
				System.out.format("@%o ",salida);
			if (((Registros.getInstancia().getAX()& 0x0000001) ==  0X00000001)) 
				System.out.format("%d ",salida);;		
			if (saltoLinea)
				System.out.format("\n");
		}
}
	
public void breakpoints(int vopA) {
	if (getParametrod()) breakParametroC();
	if (getParametrob()) breakParametroB();		
	if (getParametroc() && (vopA >> 28) != 1) clearScreen();
}

public void breakParametroC(){
	int auxiliar,start,end,ip = Registros.getInstancia().getReg(5);
	if (ip>=5) {
		start = ip - 5;
		end = ip + 5;
	}
	else {
		start = 0;
		end = 9;
	}
	System.out.print("\nCODIGO: \n");
	for (auxiliar = start;auxiliar<end;auxiliar++) {
		if (auxiliar + 1 == ip)
			System.out.print(">");
		else
			System.out.print(" ");
		System.out.format ("[%04d]: %08X  %d: ",auxiliar,Memoria.getInstancia().getValorRAM(auxiliar),(auxiliar-start+1));
		muestraInstruccion(Memoria.getInstancia().getValorRAM(auxiliar));
		System.out.print("     ");
		muestraOperandos(Memoria.getInstancia().getValorRAM(auxiliar));
		System.out.print("\n");
	}
	System.out.print("\nREGISTROS: \n");
	Registros.getInstancia().getReg(0);
	System.out.format("DS  = %d |",Registros.getInstancia().getReg(0));
	System.out.format("SS  = %d |",Registros.getInstancia().getReg(1));
	System.out.format("ES  = %d |",Registros.getInstancia().getReg(2));
	System.out.format("CS  = %d |\n",Registros.getInstancia().getReg(3));
	System.out.format("HP  = %d |",Registros.getInstancia().getReg(4));
	System.out.format("IP  = %d |",Registros.getInstancia().getReg(5));
	System.out.format("SP  = %d |",Registros.getInstancia().getReg(6));
	System.out.format("BP  = %d |\n",Registros.getInstancia().getReg(7));
	System.out.format("CC  = %d |",Registros.getInstancia().getReg(8));
	System.out.format("AC  = %d |",Registros.getInstancia().getReg(9));
	System.out.format("EAX = %d |",Registros.getInstancia().getReg(10));
	System.out.format("EBX = %d |\n",Registros.getInstancia().getReg(11));
	System.out.format("ECX = %d |",Registros.getInstancia().getReg(12));
	System.out.format("EDX = %d |",Registros.getInstancia().getReg(13));
	System.out.format("EEX = %d |",Registros.getInstancia().getReg(14));
	System.out.format("EFX = %d |\n",Registros.getInstancia().getReg(15));
}

public void breakParametroB(){

int cod;
String aux;
Scanner leer = new Scanner(System.in);
	System.out.format("[%04d] cmd: ",Registros.getInstancia().getReg(5)-1);
	aux = leer.nextLine();
	cod = codEntradaCmd(aux);
	if ( cod == 1)
		MaquinaVirtual.setP(false);
	else
		if (cod == 2)
			MaquinaVirtual.setP(true);
		else {
			if (cod == 3) {
				int i = Integer.parseInt(aux);
				System.out.format("[%04d] %08X  %d \n",i,Memoria.getInstancia().getValorRAM(i),Memoria.getInstancia().getValorRAM(i));
			}
			else 
				if (cod == 4) {  
					String op1 = primeraDireccion(aux),op2 = segundaDireccion(aux);
					int i = Integer.parseInt(op1),j = Integer.parseInt(op2);
					for (int t=i;t<=j;t++) 
						System.out.format("[%04d] %08X  %d \n",t,Memoria.getInstancia().getValorRAM(t),Memoria.getInstancia().getValorRAM(t));
				}
				else 
					if (cod == 5){}
		}
	leer.close();
}


public void clearScreen () {
	try {
		new ProcessBuilder("cmd","/c","cls").inheritIO().start().waitFor();
	}catch (Exception e) {}
}

	public void jmp (int topA,int vopA) {
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
		if (topA == 1 ) {
			if ( aRegistroA == 2) 
				valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
			else
				valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
		}
		else {
			if (topA == 2 )    //directo 
				valor = Memoria.getInstancia().getValorRAM(vopA);
			else
				if (topA == 3) 					//Indirecto
					valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
				else
					valor = vopA;
		}
		Registros.getInstancia().setIP(valor);
	}
	
	public void jz (int topA,int vopA) { 
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
		if (Registros.getInstancia().ceroCC() == 1) {
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo 
					valor = Memoria.getInstancia().getValorRAM(vopA);
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setIP(valor);
		}
	}
	
	public void jp (int topA,int vopA) { 
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
		if (Registros.getInstancia().signoCC() == 0) {
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo 
					valor = Memoria.getInstancia().getValorRAM(vopA);
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setIP(valor);
		}
	}
	
	public void jn (int topA,int vopA) { 
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
		if (Registros.getInstancia().signoCC() == 1) {
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo 
					valor = Memoria.getInstancia().getValorRAM(vopA);
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setIP(valor);
		}
	}
	
	public void jnn (int topA,int vopA) { 
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
		if (Registros.getInstancia().signoCC() == 0 || Registros.getInstancia().ceroCC()==1) {
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo 
					valor = Memoria.getInstancia().getValorRAM(vopA);
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setIP(valor);
		}
	}
	
	public void jnz (int topA,int vopA) { 
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
		if (Registros.getInstancia().ceroCC() == 0) {
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo 
					valor = Memoria.getInstancia().getValorRAM(vopA);
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setIP(valor);
		}
	}	
	
	public void jnp (int topA,int vopA) { 
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
		if (Registros.getInstancia().signoCC() == 1 || Registros.getInstancia().ceroCC() == 1) {
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo 
					valor = Memoria.getInstancia().getValorRAM(vopA);
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setIP(valor);
		}
	}

	public void ldh (int topA,int vopA) {
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo
					valor = Memoria.getInstancia().getValorRAM(vopA);         //SOLO VOPA ?????
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setAC(((valor & 0X0000FFFF)<<16)|((Registros.getInstancia().getAC() & 0X0000FFFF)));
	}

	public void ldl (int topA,int vopA) {
		int valor;
		int aux = vopA;
		int aRegistroA = (aux>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo 
					valor =Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDS());
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setAC((valor & 0X0000FFFF)|(Registros.getInstancia().getAC() & 0XFFFF0000));
	}
	
	public void not (int topA,int vopA) {
		int aux = vopA;
		int aRegistroA = (aux>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
			if (topA == 1 ) {
				if ( aRegistroA == 2)
					Registros.getInstancia().modificaReg(vopA & maskf, ~((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8));
				else
					Registros.getInstancia().modificaReg(vopA & maskf, ~(Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro));
			}
			else
				if (topA == 3) 					//Indirecto
					Memoria.getInstancia().modificaRAM(Memoria.getInstancia().getValorRAM(validarSegmento(vopA)), ~Memoria.getInstancia().getValorRAM(validarSegmento(vopA)));
				else  
					Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDS(),~ Memoria.getInstancia().getValorRAM(vopA + Registros.getInstancia().getDS()));
	}
	
	public void stop() {
		System.exit(0);
	}
	
	public void smov(int topA, int topB, int vopA, int vopB){
		int b =valor2(topB,vopB);
		int cont=0;
		System.out.println(vopA);
		while (b > 0) {
			if (topA == 2)          // Directo 
				Memoria.getInstancia().modificaRAM(vopA+ cont + Registros.getInstancia().getDSLow(),b);
			else
				if (topA == 3) 					//Indirecto
					Memoria.getInstancia().modificaRAM(validarSegmento(vopA),b);
			cont++;
			b = valor2(topB,vopB+cont);
		}
	}
	
	public void slen(int topA, int topB, int vopA, int vopB){
		int b =valor2(topB,vopB), copia = vopA, accesoA = copia>>4;
		int maskAccRegistro = accesoRegistro(accesoA);
		int nuevovalor,numreg,cont=0;
		while (b > 0) {
			cont++;
			b = valor2(topB,vopB+cont);
		}
		if (topA == 1) { //Registro
			numreg=vopA & maskf;		
			if (accesoA == 2) { //modifico valor registro (caso 3byte)		
				nuevovalor=(Registros.getInstancia().getReg(numreg)&~maskAccRegistro)| (cont<<8 & maskAccRegistro);		
			}
			else 
				nuevovalor=((Registros.getInstancia().getReg(vopA & maskf)& ~maskAccRegistro))| (cont & maskAccRegistro);
			Registros.getInstancia().modificaReg(numreg, nuevovalor);
		}
		else{
			if (topA == 2)          // Directo 
				Memoria.getInstancia().modificaRAM(vopA + Registros.getInstancia().getDSLow(),cont);
			else
				if (topA == 3) 					//Indirecto
					Memoria.getInstancia().modificaRAM(validarSegmento(vopA),cont);
		}
	}
	
public void scmp(int topA, int topB, int vopA, int vopB){
int b=-1,cont=0,res = 0, aux=-1;
	while ((res==0) && (b!=0) && (aux!=0)){
		b = valor2(topB,vopB+cont);
		if (topA == 2)         // Directo 
			aux = Memoria.getInstancia().getValorRAM(vopA+ cont + Registros.getInstancia().getDSLow());
		else 					//Indirecto
			aux = Memoria.getInstancia().getValorRAM(validarSegmento(vopA+cont));
		res = aux - b;
		cont++;
		if (res<0) {
			Registros.getInstancia().setMenosSignificativoCC(0);
			Registros.getInstancia().setMasSignificativoCC(1);
		}
		else 
			if (res == 0) {
				Registros.getInstancia().setMasSignificativoCC(0);
				Registros.getInstancia().setMenosSignificativoCC(1);
			}
			else {
				Registros.getInstancia().setMasSignificativoCC(0);
				Registros.getInstancia().setMenosSignificativoCC(0);
			}
	} 
}
	
	
public void push (int topA,int vopA) {
	int valor=0;
	int aRegistroA = (vopA>>4);
	int maskAccRegistro = accesoRegistro(aRegistroA);
		if ((Registros.getInstancia().getSP() & 0xffff) == 0) {
			System.out.print("Stack Overflow");
			stop();
		}
		else {
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else {
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
				}
			}
			else {
				if (topA == 2 )    //directo
					valor = Memoria.getInstancia().getValorRAM(vopA);         
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
		}
		Registros.getInstancia().setSP(Registros.getInstancia().getSP()-1);
		Memoria.getInstancia().modificaRAM(((Registros.getInstancia().getSP()&0XFFFF) + (Registros.getInstancia().getSS()&0xFFFF)), valor);
	}

	public void pop (int topA,int vopA) {
		int valor=0;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
			if ((Registros.getInstancia().getSP() & 0xffff) == ((Registros.getInstancia().getSS()>>16)& 0xffff)) {
				System.out.print("Stack Underflow");
				stop();
			}
			else {
				valor = Memoria.getInstancia().getValorRAM((Registros.getInstancia().getSP()&0xffff) + (Registros.getInstancia().getSS()&0xffff));
				Registros.getInstancia().setSP(Registros.getInstancia().getSP()+1);
				if (topA == 1 ) {
					if ( aRegistroA == 2) 
						Registros.getInstancia().modificaReg((vopA & maskf & maskAccRegistro)>>8,valor);
					else 
						Registros.getInstancia().modificaReg((vopA & maskf) & maskAccRegistro,valor);
				}
				else {
					if (topA == 2 )    //directo
						Memoria.getInstancia().modificaRAM(vopA, valor);         
					else
						if (topA == 3) 					//Indirecto
							Memoria.getInstancia().modificaRAM(validarSegmento(vopA),valor);
				}
			}
	}
	
	public void rnd (int topA,int vopA) {
		int valor;
		int aRegistroA = (vopA>>4);
		int maskAccRegistro = accesoRegistro(aRegistroA);
			if (topA == 1 ) {
				if ( aRegistroA == 2) 
					valor = ((Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro)>>8);
				else
					valor = (Registros.getInstancia().getReg(vopA & maskf) & maskAccRegistro);
			}
			else {
				if (topA == 2 )    //directo
					valor = Memoria.getInstancia().getValorRAM(vopA);        
				else
					if (topA == 3) 					//Indirecto
						valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopA));
					else
						valor = vopA;
			}
			Registros.getInstancia().setAC((int) Math.round(valor*Math.random()));
	}
	

	public void call (int topA,int vopA) {
		push(topA,Registros.getInstancia().getIP());
		Registros.getInstancia().setIP(vopA);

	}
	
	public void ret () {
		pop(1,5);
	}
	
	public void lecturaString(){
		int pos=  Registros.getInstancia().getEDX() & 0xffff + inicioSegmento(Registros.getInstancia().getEDX()), q=0;
		char valor;
		boolean promt = ((Registros.getInstancia().getEAX()>>11)==0);
		String aux;
		Scanner leer = new Scanner(System.in);
		if (promt) 
			System.out.format("[%04d] ",pos);
		aux = leer.nextLine();
		while (q<Registros.getInstancia().getCX()-1 && q<aux.length()) {
			valor = aux.charAt(q);
			Memoria.getInstancia().modificaRAM(pos++,(int)valor);
			q++;
		}
		valor = '\0';
		Memoria.getInstancia().modificaRAM(pos, valor);
		leer.close();
	}

	public void escrituraString(){
		int pos=  Registros.getInstancia().getEDX() & 0xffff + inicioSegmento(Registros.getInstancia().getEDX());
		boolean promt = ((Registros.getInstancia().getEAX()>>11)==0), endline = ((Registros.getInstancia().getEAX()>>8)==0);
		while (Memoria.getInstancia().getValorRAM(pos) != '\0') {
			if (promt) 
				System.out.format("[%04d] ",pos);
			System.out.format("%c\n",Memoria.getInstancia().getValorRAM(pos++));
		}
		if (endline)
			System.out.println("\n");
	}
	
	
public void ejecutaInstruccion(int a) throws InterruptedException, IOException {
	int vopA,vopB,topA,topB;
	if ((a>>28 & maskf)!= maskf){
		vopA = (a>>12) & 0x00000fff;
		vopB = a & 0x00000fff;
		topA = (a & 0X0C000000)>>26;
		topB = (a & 0X03000000)>>24;  
        switch (a>>28 & maskf){
            case 0: mov(topA, topB, vopA, vopB); break;
            case 1: add(topA, topB, vopA, vopB);break;
            case 2: sub(topA, topB, vopA, vopB);break;
            case 3: swap(topA, topB, vopA, vopB);break;
            case 4: mul(topA, topB, vopA, vopB);break;
            case 5: div(topA, topB, vopA, vopB);break;
            case 6: cmp(topA, topB, vopA, vopB);break;
            case 7: shl(topA, topB, vopA, vopB);break;
            case 8: shr(topA, topB, vopA, vopB);break;
            case 9:and(topA, topB, vopA, vopB);break;
            case 10:or(topA, topB, vopA, vopB);break;
            case 11:xor(topA, topB, vopA, vopB);break;
            case 12:slen(topA, topB, vopA, vopB);break;
            case 13:smov(topA, topB, vopA, vopB);break;
            case 14:scmp(topA, topB, vopA, vopB);break;
        }
    }
    else{
        if (((a>>24 & maskf) == maskf) && ((a>>28 & maskf) == maskf)) {
        	switch (a>>20 & maskf){
            case 1:stop();break;
            case 0:ret();break;
        	}
        }
        else{
      	   topA = (a & 0X00C00000)>>22;
      	   vopA = a & 0X0000ffff;
            switch (a>>24 & maskf){
                case 0:sys(vopA);break;
                case 1:jmp(topA,vopA);break;
                case 2:jz(topA,vopA);break;
                case 3:jp(topA,vopA);break;
                case 4:jn(topA,vopA);break;
                case 5:jnz(topA,vopA);break;
                case 6:jnp(topA,vopA);break;
                case 7:jnn(topA,vopA);break;
                case 8:ldl(topA,vopA);break;
                case 9:ldh(topA,vopA);break;
                case 10:rnd(topA,vopA);break;
                case 11:not(topA,vopA);break;
                case 12:push(topA,vopA);break;
                case 13:pop(topA,vopA);break;
                case 14:call(topA,vopA);break;
            }   
        }
    }
}

	public int valor2 ( int topB,int vopB) {
		int valor=0;
		int copiaB = vopB;
		if (topB == 1){      //Registro
			int aRegistroB = (copiaB>>4);
			int mask1 = accesoRegistro(aRegistroB);
			if (aRegistroB == 2) {
				valor = (Registros.getInstancia().getReg(vopB & maskf) & mask1)>>8;
				mask1 = mask1>>8;
				valor = valor &  0x000000FF;
			}
			else
				valor = Registros.getInstancia().getReg(vopB & maskf) & mask1;
			
			if (esNegativo(valor,mask1)) {
				valor = valor | ~mask1;
			}
		}
		else
			if (topB == 2){ // Directo
				valor=Memoria.getInstancia().getValorRAM(vopB + Registros.getInstancia().getDSLow()); //obtengo direccion de memoria
			}
			else{
				if (topB == 3) {  // Indirecto
					valor = Memoria.getInstancia().getValorRAM(validarSegmento(vopB));	
				}
				else     // tipB = 0 valor inmediato
					if (copiaB>>11 == 1) {
						valor = vopB | 0xFFFFF000;
				}
				else
				valor = vopB;
			}
		return valor;
	}
	
public int validarSegmento(int vopB) {
int valor = 0,maskff = 0x000000ff,aux,aux2,offset = (vopB>>4) & maskff;
	if (esNegativo(offset,maskff)) 
		offset = offset | ~maskff;
	aux = Registros.getInstancia().getReg(vopB & maskf)+(offset);  //valor de registro + offset
	aux2 = aux & 0xffff;
	if((aux2 + inicioSegmento(aux))>= inicioSegmento(aux) && (aux2 + inicioSegmento(aux))<= finSegmento(aux)) {
		valor = (aux2 + inicioSegmento(aux)); 
	}
	else {
		System.out.println("SEGMENTATION FAULT");
		stop();
	}
	return valor;
}
	
	public int inicioSegmento(int vop) {
		int aux = 0;
		switch ((vop>>16)& maskf) {
			case 0: aux = Registros.getInstancia().getDS() & 0x0000ffff; break;
			case 1: aux = Registros.getInstancia().getSS() & 0x0000ffff; break;
			case 2: aux = Registros.getInstancia().getES() & 0x0000ffff; break;
			case 3: aux = Registros.getInstancia().getCS() & 0x0000ffff; break;
		}
		return aux;
	}
	
	public int finSegmento(int vop) {
		int aux = 0;
		switch ((vop>>16)& maskf) {
			case 0: aux = (Registros.getInstancia().getDS() & 0x0000ffff) + ((Registros.getInstancia().getDS()>>16) & 0x0000ffff); break;
			case 1: aux = (Registros.getInstancia().getSS() & 0x0000ffff) + ((Registros.getInstancia().getSS()>>16) & 0x0000ffff); break;
			case 2: aux = (Registros.getInstancia().getES() & 0x0000ffff) + ((Registros.getInstancia().getES()>>16) & 0x0000ffff); break;
			case 3: aux = (Registros.getInstancia().getCS() & 0x0000ffff) + ((Registros.getInstancia().getCS()>>16) & 0x0000ffff); break;
		}
		return aux;
	}

	int  accesoRegistro (int a) {
		int mask=0;
		switch (a) {
		case 0: mask = 0xFFFFFFFF;break;
		case 1: mask = 0x000000FF;break;
		case 2: mask = 0x0000FF00;break;
		case 3: mask = 0x0000FFFF;break;
		}
		return mask; 
	}
	

	public int bitMasSignificativo(int mask) {
		int aux=0;
		switch (mask) {
		case 0xFFFFFFFF: aux = 31;break;
		case 0x000000FF: aux = 7;break;
		case 0x0000FF00: aux = 15;break;
		case 0x0000FFFF: aux = 15;break;
		case 0X00000FF0: aux = 11;break;
		}
		return aux; 
	}
	
	public boolean esNegativo(int valor, int mask) {
		return (((valor >>(bitMasSignificativo(mask)))& 0x1) == 1);
	}
	
	public void seteaCC (int resultado) {
		if (resultado == 0) 
			Registros.getInstancia().setMenosSignificativoCC(1);
		else 
			Registros.getInstancia().setMenosSignificativoCC(0);
		if (resultado < 0) 
			Registros.getInstancia().setMasSignificativoCC(1);
		else
			Registros.getInstancia().setMasSignificativoCC(0);
	}
	
	public void escribeRegistro(int a ) {
		switch (a & 0Xf) {
		case 10:{switch ((a & 0X00000030)>>4) {
				case 0 : System.out.print("EAX");break;
				case 1 : System.out.print("AL");break;
				case 2 : System.out.print("AH");break;
				case 3 : System.out.print("AX");break;
			}
		break;
		}
		case 11:{
			switch ((a & 0X00000030)>>4) {
			case 0 : System.out.print("EBX");break;
			case 1 : System.out.print("BL");break;
			case 2 : System.out.print("BH");break;
			case 3 : System.out.print("BX");break;
		}
		break;	
		}
		case 12:{
			switch ((a & 0X00000030)>>4) {
			case 0 : System.out.print("ECX");break;
			case 1 : System.out.print("CL");break;
			case 2 : System.out.print("CH");break;
			case 3 : System.out.print("CX");break;
		}
		break;	
		}
		case 13:{
			switch ((a & 0X00000030)>>4) {
			case 0 : System.out.print("EDX");break;
			case 1 : System.out.print("DL");break;
			case 2 : System.out.print("DH");break;
			case 3 : System.out.print("DX");break;
		}
		break;
		}
		case 14:{
			switch ((a & 0X00000030)>>4) {
			case 0 : System.out.print("EEX");break;
			case 1 : System.out.print("EL");break;
			case 2 : System.out.print("EH");break;
			case 3 : System.out.print("EX");break;
		}
		break;
		}
		case 15:{
			switch ((a & 0X00000030)>>4) {
			case 0 : System.out.print("EFX");break;
			case 1 : System.out.print("FL");break;
			case 2 : System.out.print("FH");break;
			case 3 : System.out.print("FX");break;
		}
		break;
		}
		}
	}

	public void muestraOperandos(int a) {
		int vopA,vopB,topA,topB;
		vopA = (a>>12) & 0x00000fff;
		topA = (a & 0X0C000000)>>26;
		topB = (a & 0X03000000)>>24;
		vopB = a & 0x00000fff;
		if ((a>>28 & maskf)!= maskf){
			if (topA == 1){
				escribeRegistro(vopA);
			}
			else {
				if (topA == 2) {
					System.out.print("["+vopA+"]");
				}
				else
					System.out.print(vopA);
			}
			System.out.print(",");
			if (topB == 1){
				escribeRegistro(vopB);
			}
			else {
				if (topB == 2) {
					System.out.print("["+vopB+"]");
				}
				else
					System.out.print(vopA);
			}	
	    }
	    else{
	        if (((a>>24 & maskf) == maskf) && ((a>>28 & maskf) == maskf)) {
	        }
	        else{
	      	   topA = (a & 0X00C00000)>>22;
	      	   vopA = a & 0X0000ffff;
	      	 if (topA == 1){
	 			escribeRegistro(vopA);
	 		}
	 		else {
	 			if (topA == 2) {
	 				System.out.print("["+vopA+"]");
	 			}
	 			else
	 				System.out.print(vopA);
	 		}
	       }
	    }
	}


	public void muestraInstruccion(int a) {
		if ((a>>28 & maskf)!= maskf){   
	        switch (a>>28 & maskf){
	            case 0: System.out.print("MOV");break;
	            case 1: System.out.print("ADD");break;
	            case 2: System.out.print("SUB");break;
	            case 3: System.out.print("SWAP");break;
	            case 4: System.out.print("MUL");break;
	            case 5: System.out.print("DIV");break;
	            case 6: System.out.print("CMP");break;
	            case 7: System.out.print("SHL");break;
	            case 8: System.out.print("SHR");break;
	            case 9: System.out.print("AND");break;
	            case 10: System.out.print("OR");break;
	            case 11: System.out.print("XOR");break;
	            case 12: System.out.print("SLEN");break;
	            case 13: System.out.print("SMOV");break;
	            case 14: System.out.print("SCMP");break;
	        }
	    }
	    else{
	        if (((a>>24 & maskf) == maskf) && ((a>>28 & maskf) == maskf))
	        	if (a>>20 == 1)
	        		System.out.print("STOP\n");
	        	else
	        		System.out.print("RET\n");
	        else{
	      	   
	            switch (a>>24 & maskf){
	                case 0:System.out.print("SYS");break;
	                case 1:System.out.print("JMP");break;
	                case 2:System.out.print("JZ");break;
	                case 3:System.out.print("JP");break;
	                case 4:System.out.print("JN");break;
	                case 5:System.out.print("JNZ");break;
	                case 6:System.out.print("JNP");break;
	                case 7:System.out.print("JNN");break;
	                case 8:System.out.print("LDL");break;
	                case 9:System.out.print("LDH");break;
	                case 11:System.out.print("NOT");break;
	                case 12: System.out.print("PUSH");break;
	                case 13: System.out.print("POP");break;
	                case 14: System.out.print("CALL");break;
	            }   
	        }
	    }
	}
	
	
	public int codEntradaCmd (String cadena) {
		int cod,w;
		boolean espacio= false;
		if (cadena.compareTo("r")==0)
			cod = 1;
		else
			if(cadena.compareTo("p")==0) 
				cod = 2;
			else
				if(cadena.compareTo(" ")==0)
					cod = 5;
				else{
					w=0;
					while (!espacio && w< cadena.length()) {
						if (cadena.charAt(w)==' ') {
							espacio = true;
						}
						w++;	
					}
			if (espacio)
				cod = 4;
			else
				cod = 3;
		}
	return cod;
	}
	
	public String primeraDireccion (String cadena) {
		int w=0;
			while (cadena.charAt(w)!=' ')
				w++;
		return cadena.substring(0,w);
	}
	
	public String segundaDireccion (String cadena) {
		int w=0;
			while (cadena.charAt(w)!=' ') 
				w++;
		return cadena.substring(w+1,cadena.length());
	}
}
