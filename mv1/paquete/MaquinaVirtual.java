package paquete;

import java.io.IOException;

public class MaquinaVirtual {

	private Memoria memoria;
	private Registros registros;
	private ALU alu;
	
	private static boolean p = false;
	public boolean isP() {
		return p;
	}
	
	public boolean isBreakpoint(int inst) {
		return inst == 0xF000000F;
	}
	
	public void sys() throws InterruptedException, IOException {
		this.alu.sys(0x1000000F);	// se fuerza el valor de un sys %F y el 1 es para evitar que se limpie la pantalla
	}
	
	public static void setP(boolean p) {
		MaquinaVirtual.p = p;
	}
	
	public MaquinaVirtual() {
	
		this.memoria = Memoria.getInstancia();
		this.registros = Registros.getInstancia();
		this.alu = new ALU();
	
	}
	
	public void setParametrob(boolean paramB) {
        alu.setParametroB(paramB);
    }
	
    public  void setParametroc(boolean paramC) {
        alu.setParametroC(paramC);
    }
    
    public  void setParametrod(boolean paramD) {
        alu.setParametroD(paramD);
    }

	public void cargainstruccion(int instruccion,int i) {
		this.memoria.cargainstruccion(instruccion,i);
	}
	
	public int getInstruccion() {
		return this.memoria.getInstruccion(Registros.getInstancia().getIPLow()+Registros.getInstancia().getCSLow());
	}
	
	public void ejecutaInstruccion(int instruccion) throws InterruptedException, IOException {
		alu.ejecutaInstruccion(instruccion);
	}
	
	public int getDS() {
		return this.registros.getDS();
	}
	
	public int getDSLow() {
		return this.registros.getDSLow();
	}
	public int getDSHigh() {
		return this.registros.getDSHigh();
	}
	public int getCSHigh() {
		return this.registros.getCSHigh();
	}
	public int getIP() {
		return this.registros.getIP();
	}
	public int getIPLow() {
		return this.registros.getIPLow();
	}
	
	public void incrementaIP() {
		this.registros.incrementaIP();
	}
	
	public int getEAX() {
		return this.registros.getAX();
	}
	
	public void setIP(int valor) {
		this.registros.setIP(valor);
	}
	public void setInicialIP(int valor) {
		this.registros.setInicialIP(valor);
	}
	
	public void setregistroo(String nombre,int valor) {
		registros.setRegistro(nombre, valor);
	}
	
	public int devuelveCelda(int numcelda) {
		return memoria.getValorRAM(numcelda);
	}
	
	public boolean esNegativoo(int numero) {
		return this.alu.esNegativo(numero,0xFF);
	}
	
	public void setDS(int DS) {
		this.registros.setDS(DS);
	}
	public void setES(int aux) {
		this.registros.setES(aux);
	}
	public void setSS(int aux) {
		this.registros.setSS(aux);
	}
	public void setCS(int aux) {
		this.registros.setCS(aux);
	}
	public void setSP(int aux) {
		this.registros.setSP(aux);
	}
	public void setHP(int aux) {
		this.registros.setHP(aux);
	}
	public void setInicialBP(int aux) {
		this.registros.setInicialBP(aux);
	}
}