package main;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import paquete.ALU;
import paquete.MaquinaVirtual;
import paquete.Memoria;
import paquete.Registros;

public class Main { 

	public static void main(String[] args) throws IOException, InterruptedException {

		int i, inst, CS = 0;
		String binFilename = null;
		
		MaquinaVirtual maquinavirtual = new MaquinaVirtual();
		for (i = 0; i < args.length; i++) {
            if (args[i].endsWith("-b"))
                maquinavirtual.setParametrob(true);
            else if (args[i].endsWith("-c")) {
                maquinavirtual.setParametroc(true);
            }
            else if (args[i].endsWith("-d"))
                maquinavirtual.setParametrod(true);
            else if (args[i].endsWith(".mv1"))
                binFilename = args[i];
        }
		
		if (binFilename != null) {	// Si encuentra el archivo
			FileInputStream arch = new FileInputStream(binFilename);
			DataInputStream entrada = new DataInputStream(arch);

			for (i = 0; i < 6; i++) {					// lee el header
				inst = entrada.readInt();
				if (i == 1) {		// el 2do bloque tiene el tama�o del codigo 
					CS = inst;
				}
			}
			
			// Esto lo deberia hacer al leer el archivo binario
			
			maquinavirtual.setDS(0x000a0000 + CS);
			maquinavirtual.setES(0x0BB80023);
			maquinavirtual.setSS(0x13880BDB);
			maquinavirtual.setCS(0x00190000);
			maquinavirtual.setSP(0x00011388);
			maquinavirtual.setHP(0x00020000);
			maquinavirtual.setIP(0x00030000);
			maquinavirtual.setBP(0x00010000);
			
			for (i = 0; i < CS; i++) {					// carga instrucciones en la RAM
				inst = entrada.readInt();
				maquinavirtual.cargainstruccion(inst, i);
			}
			arch.close();
			entrada.close();
			
			if (ALU.getParametroc())
				try {
					new ProcessBuilder("cmd","/c","cls").inheritIO().start().waitFor();
				}catch (Exception e) {
					
				}


			do {										// lee y ejecuta codigo desde la RAM
				inst = maquinavirtual.getInstruccion();
				maquinavirtual.incrementaIP();
				maquinavirtual.ejecutaInstruccion(inst);
				if (maquinavirtual.isP() && !maquinavirtual.isBreakpoint(inst))
					maquinavirtual.sys();
			} while ((0 <= (maquinavirtual.getIPLow())) && ((maquinavirtual.getIPLow()) < (maquinavirtual.getDSLow())));
		} else 
			System.out.println("El archivo .mv1 a leer no se encontr� en la lista de parametros");
	}
}

