package paquete;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.SecureRandom;

public class UnidadDisco {

	static int contadorDisco=0;
	private int numdisco;
	private int cabezas,cilindros,sectores,tamañosector; //Se leen del archivo
	private RandomAccessFile archivo;
	

	
	public UnidadDisco(String archivo){
		try {
			this.numdisco=contadorDisco;
			contadorDisco++;
			lecturaHeader(archivo);
		}
		catch (IOException e) {
			System.out.println("No existe el archivo y hubo error para crearlo");
		}

		
	}

	public void lecturaHeader(String archivo) throws IOException {
		
		RandomAccessFile file = new RandomAccessFile(archivo, "rw"); //Si existe el archivo lo abre, sino lo crea
		if (file.length()==0) { //Si no existia el archivo
			CompletaHeader(file);
		}
		else { //Si ya existia el archivo

			this.archivo=file;
			int i,lectura;
				for (i = 0; i < 128; i++) {	// lee el header (128 celdas de 4 bytes)
					
					lectura=file.readInt();
					
					if (i==8) { //datos disco
						this.sectores= lectura & 0XFF;
						this.cabezas= lectura >> 8 & 0XFF;
						this.cilindros = lectura >> 16 &  0xFF;
						
					}
					else if (i==9) { //tamaño sector
						this.tamañosector=lectura;
					}
				}
		}


	}
	
	
	public void CompletaHeader(RandomAccessFile file) {
		try {
			this.cilindros=128;
			this.cabezas=128;
			this.sectores=128;
			this.tamañosector=512;
			//Creo el archivo VDD con el header (con valores por defecto)
			
			file.writeInt(362955776); //VDD0 en decimal
			file.writeInt(1); //NUM VERSION
			
			SecureRandom random = new SecureRandom();
			byte identificadorDiscoBin[] = new byte[16];
			random.nextBytes(identificadorDiscoBin);
			file.write(identificadorDiscoBin); //Identificador disco 16 bytes
			
			file.writeInt(20); //fecha 
			file.writeInt(20); //hora
			

			int infodisco = 1; //tipo disco (FIJO = 1)
			infodisco = infodisco << 8 | 128; //cilindros
			infodisco = infodisco << 8 | 188; //cabezas
			infodisco = infodisco << 8 | 128; //Sector
			file.writeInt(infodisco);
			file.writeInt(512); //tamaño del sector
			
			file.setLength(512);
			
			this.archivo=file;
		
		}
		catch(IOException e) {
			System.out.println("Error para crear el archivo");
		}
	}

	//Getters
	
	public int getTamañosector() {
		return tamañosector;
	}
	
	
	public int getNumdisco() {
		return numdisco;
	}

	
	public RandomAccessFile getArchivo() {
		return archivo;
	}


	public int getCabezas() {
		return cabezas;
	}

	public int getCilindros() {
		return cilindros;
	}

	
	public int getSectores() {
		return sectores;
	}


	
}