package es.joseoc.learning.wiremock.io.http.exceptions;

public final class UnsupportedSourceEncodingException extends RuntimeException {
	private static final long serialVersionUID = -1677997111063466049L;

	public UnsupportedSourceEncodingException(String encoding, Exception e) {
		super("Unsupported Source Encoding " + encoding, e);
	}

}
