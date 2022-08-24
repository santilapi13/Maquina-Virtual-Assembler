package excepciones;

@SuppressWarnings("serial")
public class ParamDiscoIncorrectoException extends Exception {
	
	private String motivoError;
	
	public ParamDiscoIncorrectoException(String error) {
		this.motivoError=error;
	}

	public String getMotivoError() {
		return motivoError;
	}
	
}
