package es.joseoc.learning.wiremock;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Simulator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Simulator.class);
	
	private HttpClient httpClient = HttpClientBuilder.create().build();

	public HttpResponse performIncorrectPost(String url) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type", "json");

		HttpEntity entity = new StringEntity("{\"p\": \"UNEXPECTED_VALUE\"}");
		post.setEntity(entity);
		HttpResponse response = httpClient.execute(post);
		
		LOGGER.info("Response: " + response);
		return response;
	}
		
	public HttpResponse performCorrectPost(String url) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type", "json");

		HttpEntity entity = new StringEntity("{\"p\": \"v\"}");
		post.setEntity(entity);
		HttpResponse response = httpClient.execute(post);
		
		LOGGER.info("Response: " + response);
		return response;
	}

}
