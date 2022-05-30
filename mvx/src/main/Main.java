package main;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import paquete.ALU;
import paquete.MaquinaVirtual;
import paquete.Memoria;
import paquete.Registros;

public class Main {
	
	private static String[] argsCopy;
	
	public static String[] getArgs() {
        return argsCopy;
    }

	public static void main(String[] args) throws IOException, InterruptedException {
		
		argsCopy=args;
		int[] header = new int[6];
		int i, a, inst;
		String binFilename = null;

		MaquinaVirtual maquinavirtual = new MaquinaVirtual();
		for (i = 0; i < args.length; i++) {
			if (args[i].endsWith("-b"))
				maquinavirtual.setParametrob(true);
			else if (args[i].endsWith("-c")) {
				maquinavirtual.setParametroc(true);
			} else if (args[i].endsWith("-d"))
				maquinavirtual.setParametrod(true);
			else if (args[i].endsWith(".mv1"))
				binFilename = args[i];
		}

		if (binFilename != null) { // Si encuentra un nombre de archivo
			FileInputStream arch = new FileInputStream(binFilename);
			DataInputStream entrada = new DataInputStream(arch);

			for (i = 0; i < 6; i++) // lee el header
				header[i] = entrada.readInt();

			if ((header[0] == 0x4d562d32) && (header[5] == 0x562e3232)) {
				if ((header[1] + header[2] + header[3]) <= (8192 - header[4])) {
					maquinavirtual.setCS((header[4] << 16) & 0xffff0000); // CS 1°
					a = header[4];
					maquinavirtual.setDS(((header[1] << 16) & 0xffff0000) + (a & 0xffff)); // DS 2°
					a = a + header[1];
					maquinavirtual.setES(((header[3] << 16) & 0xffff0000) + (a & 0xffff)); // ES 3°
					a = a + header[2];
					maquinavirtual.setSS(((header[2] << 16) & 0xffff0000) + (a & 0xffff)); // SS 4°
					maquinavirtual.setSP((0x00010000) + header[2]);
					maquinavirtual.setHP(0x00020000);
					maquinavirtual.setInicialIP(0x00030000);
					maquinavirtual.setInicialBP(0x00010000);

					for (i = 0; i < header[4]; i++) { // carga instrucciones en la RAM
						inst = entrada.readInt();
						maquinavirtual.cargainstruccion(inst, i);
					}
					arch.close();
					entrada.close();

					if (ALU.getParametroc())
						try {
							new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
						} catch (Exception e) {
						}	
					 // Carga Arbol en memoria 
					 int q=Registros.getInstancia().getDSLow();
					 Memoria.getInstancia().modificaRAM(10+q, 8);
					 Memoria.getInstancia().modificaRAM(11+q, 16);
					 Memoria.getInstancia().modificaRAM(12+q, 13);
					 Memoria.getInstancia().modificaRAM(13+q, 4);
					 Memoria.getInstancia().modificaRAM(14+q, -1);
					 Memoria.getInstancia().modificaRAM(15+q, -1);
					 Memoria.getInstancia().modificaRAM(16+q, 4);
					 Memoria.getInstancia().modificaRAM(17+q, 19);
					 Memoria.getInstancia().modificaRAM(18+q, 22);
					 
					 Memoria.getInstancia().modificaRAM(19+q, 3);
					 Memoria.getInstancia().modificaRAM(20+q, -1);
					 Memoria.getInstancia().modificaRAM(21+q, -1);
					 Memoria.getInstancia().modificaRAM(22+q, 3);
					 Memoria.getInstancia().modificaRAM(23+q, -1);
					 Memoria.getInstancia().modificaRAM(24+q, -1);
					 
					do { // lee y ejecuta codigo desde la RAM
						inst = maquinavirtual.getInstruccion();
						maquinavirtual.incrementaIP();
						maquinavirtual.ejecutaInstruccion(inst);
						if (maquinavirtual.isP() && !maquinavirtual.isBreakpoint(inst))
							maquinavirtual.sys();
					} while ((0 <= (maquinavirtual.getIPLow()))
							&& ((maquinavirtual.getIPLow()) < (maquinavirtual.getCSHigh())));
				} else
					System.out.println("El proceso no puede ser cargado por memoria insuficiente");
			} else
				System.out.println("El formato del archivo no es correcto");
		} else
			System.out.println("El archivo .mv2 a leer no se encontro en la lista de parametros");
	}
}