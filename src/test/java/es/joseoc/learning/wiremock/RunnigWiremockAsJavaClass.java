package es.joseoc.learning.wiremock;

import static org.junit.Assert.assertEquals;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public final class RunnigWiremockAsJavaClass {

	@Test
	public void basicExample() throws Exception {
		// By default it uses port 8080
		WireMockServer wireMockServer = new WireMockServer();
		wireMockServer.start();
		
		// Create the mapping 
		StubMapping stubMapping = StubMapping.buildFrom("{\"request\":{\"urlPattern\":\"/simple/mapping/to/some/get\",\"method\":\"GET\"},\"response\":{\"status\":404}}");
		wireMockServer.addStubMapping(stubMapping);

		// Perform GET to the mock mapping
		CloseableHttpResponse response = HttpClientBuilder
				.create()
				.build()
				.execute(new HttpGet("http://localhost:8080/simple/mapping/to/some/get"));

		WireMock.reset();

		// Finish doing stuff
		wireMockServer.stop();
		
		assertEquals(404, response.getStatusLine().getStatusCode());
	}
}
