package paquete;

import java.util.HashMap;

public class Registros {

	private static Registros instancia = null;

	public HashMap<String, Integer> registros = new HashMap<String, Integer>();

	private Registros() {
		registros.put("DS", 0);
		registros.put("SS", 0);
		registros.put("ES", 0);
		registros.put("CS", 0);
		registros.put("HP", 0);
		registros.put("IP", 0);
		registros.put("SP", 0);
		registros.put("BP", 0);
		registros.put("CC", 0);
		registros.put("AC", 0);
		registros.put("EAX", 0);
		registros.put("EBX", 0);
		registros.put("ECX", 0);
		registros.put("EDX", 0);
		registros.put("EEX", 0);
		registros.put("EFX", 0);
	}

	public static Registros getInstancia() {
		if (instancia == null)
			instancia = new Registros();
		return instancia;
	}

	public int getRegistro(String reg) { // Unico metodo (con setRegistro) que conoce la implementacion de registros
		return this.registros.get(reg);
	}

	public void setRegistro(String reg, int valor) {
		this.registros.replace(reg, valor);
	}

	public int getReg(int numregistro) { // Accedo al valor del registro con su numero
		switch (numregistro) {
		case 0: {
			return getRegistro("DS");
		}
		case 1: {
			return getRegistro("SS");
		}
		case 2: {
			return getRegistro("ES");
		}
		case 3: {
			return getRegistro("CS");
		}
		case 4: {
			return getRegistro("HP");
		}
		case 5: {
			return getRegistro("IP");
		}
		case 6: {
			return getRegistro("SP");
		}
		case 7: {
			return getRegistro("BP");
		}
		case 8: {
			return getRegistro("CC");
		}
		case 9: {
			return getRegistro("AC");
		}
		case 10: {
			return getRegistro("EAX");
		}
		case 11: {
			return getRegistro("EBX");
		}
		case 12: {
			return getRegistro("ECX");
		}
		case 13: {
			return getRegistro("EDX");
		}
		case 14: {
			return getRegistro("EEX");
		}
		case 15: {
			return getRegistro("EFX");
		}
		default: {
			return 1;
		}
		}
	}

	public void modificaReg(int numregistro, int nuevovalor) { // actualizo el registro completo -> La ALU ve que parte
																// modificar
		switch (numregistro) {
		case 0: {
			setRegistro("DS", nuevovalor);
			break;
		}
		case 1: {
			setRegistro("SS", nuevovalor);
			break;
		}
		case 2: {
			setRegistro("ES", nuevovalor);
			break;
		}
		case 3: {
			setRegistro("CS", nuevovalor);
			break;
		}
		case 4: {
			setRegistro("HP", nuevovalor);
			break;
		}
		case 5: {
			setRegistro("IP", nuevovalor);
			break;
		}
		case 6: {
			setRegistro("SP", nuevovalor);
			break;
		}
		case 7: {
			setRegistro("BP", ((this.getBP() & 0xffff0000) + (nuevovalor & 0xffff)));
			break;
		}
		case 8: {
			setRegistro("CC", nuevovalor);
			break;
		}
		case 9: {
			setRegistro("AC", nuevovalor);
			break;
		}
		case 10: {
			setRegistro("EAX", nuevovalor);
			break;
		}
		case 11: {
			setRegistro("EBX", nuevovalor);
			break;
		}
		case 12: {
			setRegistro("ECX", nuevovalor);
			break;
		}
		case 13: {
			setRegistro("EDX", nuevovalor);
			break;
		}
		case 14: {
			setRegistro("EEX", nuevovalor);
			break;
		}
		case 15: {
			setRegistro("EFX", nuevovalor);
			break;
		}
		}
	}

	// FUNCIONES ESPECIFICAS, LLAMO A UN REGISTRO EN PARTICULAR

	// getters
	public int getCC() {
		return this.registros.get("CC");
	}

	public int getDS() {
		return this.registros.get("DS");
	}

	public int getBP() {
		return this.registros.get("BP");
	}

	public int getDSLow() {
		return this.registros.get("DS") & 0xffff;
	}

	public int getDSHigh() {
		return (this.registros.get("DS") & 0xffff0000) >> 16;
	}

	public int getCSHigh() {
		return (this.registros.get("CS") & 0xffff0000) >> 16;
	}

	public int getCSLow() {
		return (this.registros.get("CS") & 0xffff);
	}

	public int getSS() {
		return this.registros.get("SS");
	}

	public int getES() {
		return this.registros.get("ES");
	}

	public int getCS() {
		return this.registros.get("CS");
	}

	public int getHP() {
		return this.registros.get("HP");
	}

	public int getIP() {
		return this.registros.get("IP");
	}

	public int getIPLow() {
		return this.registros.get("IP") & 0xffff;
	}

	public int getSP() {
		return this.registros.get("SP");
	}

	public int getAC() {
		return getReg(9);
	}

	public int signoCC() {
		return (getRegistro("CC") >> 31) & 0X00000001;
	}

	public int ceroCC() {
		return getRegistro("CC") & 0x00000001;
	}

	public int getAX() {
		return getRegistro("EAX") & 0X0000FFFF;
	}

	public int getEAX() {
		return getRegistro("EAX");
	}

	public int getEBX() {
		return getRegistro("EBX");
	}

	public int getEDX() {
		return getRegistro("EDX");
	}

	public int getCX() {
		return getRegistro("ECX") & 0X0000FFFF;
	}

	// setters y modificadores
	public void incrementaIP() {
		int aux = this.registros.get("IP") + 1;
		this.registros.replace("IP", aux);
	}

	public void setMenosSignificativoCC(int aux) {
		modificaReg(8, aux);
	}

	public void setMasSignificativoCC(int aux) {
		if (getReg(8) >> 31 == 1)
			modificaReg(8, getReg(8) & (aux << 31));
		else
			modificaReg(8, getReg(8) | (aux << 31));
	}

	public void setAC(int aux) {
		modificaReg(9, aux);
	}

	public void setInicialIP(int aux) {
		this.registros.replace("IP", aux);
	}

	public void setInicialBP(int aux) {
		this.registros.replace("BP", aux);
	}

	public void setIP(int aux) {
		modificaReg(5, ((this.getIP() & 0xffff0000) + (aux & 0xffff)));
	}

	public void modificaIP(int aux) {
		modificaReg(5, ((this.getIP() & 0xffff0000) + aux));
	}

	public void setES(int aux) {
		modificaReg(2, aux);
	}

	public void setDS(int aux) {
		modificaReg(0, aux);
	}

	public void setSP(int aux) {
		modificaReg(6, aux);
	}

	public void setSS(int aux) {
		modificaReg(1, aux);
	}

	public void setCS(int aux) {
		modificaReg(3, aux);
	}

	public void setBP(int aux) {
		modificaReg(7, aux);
	}

	public void setHP(int aux) {
		modificaReg(4, aux);
	}

	public void setCC(int aux) {
		modificaReg(8, aux);
	}

	public void setAH(int aux) { // modifica solo parte alta
		int nuevovalor;

		nuevovalor = (this.getRegistro("EAX") & ~0xFFFF0000) | ((aux & 0XFFFF) << 16);
		modificaReg(10, nuevovalor);
	}

	public void setAL(int aux) {
		int nuevovalor;

		nuevovalor = (this.getRegistro("EAX") & ~0xFFFF) | (aux & 0XFFFF);
		modificaReg(10, nuevovalor);
	}

	public void setCH(int aux) { // modifica solo parte alta
		int nuevovalor;

		nuevovalor = (this.getRegistro("ECX") & ~0xFFFF0000) | ((aux & 0XFFFF) << 16);
		modificaReg(12, nuevovalor);
	}

	public void setCL(int aux) {
		int nuevovalor;

		nuevovalor = (this.getRegistro("ECX") & ~0xFFFF) | (aux & 0XFFFF);
		modificaReg(12, nuevovalor);
	}

	public void setDH(int aux) { // modifica solo parte alta
		int nuevovalor;

		nuevovalor = (this.getRegistro("EDX") & ~0xFFFF0000) | ((aux & 0XFFFF) << 16);
		modificaReg(13, nuevovalor);
	}

	public void setDL(int aux) {
		int nuevovalor;

		nuevovalor = (this.getRegistro("EDX") & ~0xFFFF) | (aux & 0XFFFF);
		modificaReg(13, nuevovalor);
	}
	// nuevos getters

	public int getAH() {
		return (this.registros.get("EAX") >> 16) & 0x0000FFFF;
	}

	public int getAL() {
		return this.registros.get("EAX") & 0xFFFF;
	}

	public int getCH() {
		return (this.registros.get("ECX") >> 16) & 0xFFFF;
	}

	public int getCL() {
		return this.registros.get("ECX") & 0xFFFF;
	}

	public int getDH() {
		return (this.registros.get("EDX") >> 16) & 0xFFFF;
	}

	public int getDL() {
		return this.registros.get("EDX") & 0xFFFF;
	}
}
