package es.joseoc.learning.wiremock.io.http;

import static java.lang.Long.parseLong;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_REQUEST_TIMEOUT;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import es.joseoc.learning.wiremock.io.http.exceptions.HttpClientException;
import es.joseoc.learning.wiremock.io.http.exceptions.HttpContentNotFound;
import es.joseoc.learning.wiremock.io.http.exceptions.HttpServerException;
import es.joseoc.learning.wiremock.io.http.exceptions.UnsupportedSourceEncodingException;

public final class HttpFileReader {
	private final HttpClient httpClient;

	public HttpFileReader() {
		this.httpClient = HttpClientBuilder.create().build();
	}
	public HttpFileReader(final HttpHost proxy) {
		this.httpClient = HttpClientBuilder.create().setProxy(proxy).build();
	}

	public HttpFileReader(final HttpClient client) {
		this.httpClient = client;
	}

	public long getFileSize(URI url) throws SocketException, IOException, URISyntaxException {
		long fileSize = 0;

		HttpResponse response = performHead(url);

		manageErrorStatusCode(url, response);

		Header[] headers = response.getAllHeaders();
		for (Header header : headers) {
			if (HttpHeaders.CONTENT_LENGTH.equals(header.getName())) {
				fileSize = parseLong(header.getValue());
			}
		}

		return fileSize;
	}
	

	public Collection<String> listFiles(URI url) throws SocketException, IOException, URISyntaxException {
		Collection<String> files = new ArrayList<>();
		try {
			Document doc;
			doc = Jsoup.connect(url.toString()).get();
			for (Element element : doc.getAllElements()) {
				String hrefItem = element.attr("href");
				if (StringUtils.isNotBlank(hrefItem)) {
					files.add(hrefItem);
				}
			}
		} catch (HttpStatusException e) {
			throw new RuntimeException(String.format("Error parsing HTML. URL %s. Status code: %d ", e.getUrl(), e.getStatusCode()), e);
		}

		return files;
	}

	public String readFileAsString(URI url) throws SocketException, IOException, URISyntaxException {
		return readFileAsString(url, defaultCharset().toString());
	}

	public String readFileAsString(URI url, String encoding) throws SocketException, IOException, URISyntaxException {
		HttpResponse response = performGet(url);
		HttpEntity entity = response.getEntity();

		manageErrorStatusCode(url, response);

		InputStream is = entity.getContent();
		try {
			return IOUtils.toString(is, encoding);
		} catch (UnsupportedEncodingException | UnsupportedCharsetException | IllegalCharsetNameException e) {
			throw new UnsupportedSourceEncodingException(encoding, e);
		}
	}

	private HttpResponse performHead(URI uri) {
		try {
			HttpHead httpHead = new HttpHead(uri);
			return httpClient.execute(httpHead);
		} catch (IOException e) {
			throw new HttpServerException(uri, e);
		}
	}

	private HttpResponse performGet(URI url) {
		try {
			HttpGet httpget = new HttpGet(url);
			return httpClient.execute(httpget);
		} catch (IOException e) {
			throw new HttpServerException(url, e);
		}
	}

	private boolean noResponse(HttpResponse response) {
		return noResponse(response.getStatusLine().getStatusCode());
	}

	private boolean contentNotFound(HttpResponse response) {
		return contentNotFound(response.getStatusLine().getStatusCode());
	}

	private boolean serverError(HttpResponse response) {
		return serverError(response.getStatusLine().getStatusCode());
	}

	private boolean clientError(HttpResponse response) {
		return clientError(response.getStatusLine().getStatusCode());
	}

	private boolean noResponse(int statusCode) {
		return (statusCode == 0);
	}

	private boolean contentNotFound(int statusCode) {
		return (statusCode == SC_NOT_FOUND);
	}

	private boolean serverError(int statusCode) {
		return (statusCode >= SC_INTERNAL_SERVER_ERROR || statusCode == SC_REQUEST_TIMEOUT);
	}

	private boolean clientError(int statusCode) {
		return (statusCode >= SC_BAD_REQUEST && statusCode < SC_INTERNAL_SERVER_ERROR && statusCode != SC_REQUEST_TIMEOUT);
	}

	private void manageErrorStatusCode(URI source, HttpResponse response) {
		if (contentNotFound(response)) {
			throw new HttpContentNotFound(source, response);
		}

		if (noResponse(response) || serverError(response)) {
			throw new HttpServerException(source, response);
		}

		if (clientError(response)) {
			throw new HttpClientException(source, response);
		}
	}

}
