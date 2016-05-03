package es.joseoc.learning.wiremock.io.http.exceptions;

import java.net.URI;
import java.util.Objects;

import org.apache.http.HttpResponse;

import lombok.Getter;

@Getter
public final class HttpContentNotFound extends RuntimeException {
	private static final long serialVersionUID = -306136050349881817L;
	private final URI uri;
	private final HttpResponse response;

	public HttpContentNotFound(URI source, HttpResponse response) {
		super("Content not found in the source " + Objects.toString(source) + " returning the response: " + Objects.toString(response));
		this.uri = source;
		this.response = response;
	}

}
