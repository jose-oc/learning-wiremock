package es.joseoc.learning.wiremock.io.http.exceptions;

import java.net.URI;
import java.util.Objects;

import org.apache.http.HttpResponse;

import lombok.Getter;

@Getter
public final class HttpServerException extends RuntimeException {
	private static final long serialVersionUID = -4693182703671688732L;
	private final URI uri;
	private final HttpResponse response;

	public HttpServerException(URI source, HttpResponse response) {
		super("Error in the server. Source " + Objects.toString(source) + " returning the response: " + Objects.toString(response));
		this.uri = source;
		this.response = response;
	}

	public HttpServerException(URI uri, Exception e) {
		super("Error in the server. Source " + Objects.toString(uri), e);
		this.uri = uri;
		this.response = null;
	}

}
