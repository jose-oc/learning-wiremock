package es.joseoc.learning.wiremock.io.http.exceptions;

import java.net.URI;
import java.util.Objects;

import org.apache.http.HttpResponse;

import lombok.Getter;

@Getter
public final class HttpClientException extends RuntimeException {
	private static final long serialVersionUID = 3012473699294527163L;
	private final URI uri;
	private final HttpResponse response;

	public HttpClientException(URI source, HttpResponse response) {
		super("Client error. Source " + Objects.toString(source) + " returning the response: " + Objects.toString(response));
		this.uri = source;
		this.response = response;
	}
}
