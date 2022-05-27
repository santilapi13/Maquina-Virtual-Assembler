package paquete;

public class Memoria {
	
	public static Memoria instancia = null;
	
	public int[] memoria = new int[8192]; //cada columna tendr� 8 bits (el int puede almacenarlos)
	
	private Memoria() {}
	
	public static Memoria getInstancia() {
		if (instancia == null)
			instancia = new Memoria();
		return instancia;
	}
	
	public void cargainstruccion(int instruccion,int i) {
		
		memoria[i]=instruccion;
	}
	
	public int getInstruccion(int IP) { //obtengo la instruccion de la celda de memoria q apunta IP	
		return memoria[IP];
	}

	public void modificaRAM(int celda,int nuevovalor) {
		this.memoria[celda & 0xffff]=nuevovalor;
	}
	
	public int getValorRAM(int celda) {
		return memoria[celda & 0xffff];
	}
}