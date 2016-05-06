package es.joseoc.learning.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
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
	
	@Test
	public void testMyMethodPostCorrectly() throws Exception {
		// By default it uses port 8080
		WireMockServer wireMockServer = new WireMockServer();
		wireMockServer.start();
		
		// Create the mapping 
		stubFor(post(
				urlEqualTo("/some-path/metadata.xml"))
				.withHeader("Content-Type", containing("json"))
				.withRequestBody(equalToJson("{\"p\": \"v\"}"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withHeader("Content-Length", "33")
						.withBody("This is the body for the response")
						.withFixedDelay(1000)));
		
		Simulator simulator = new Simulator();
		
		HttpResponse response1 = simulator.performIncorrectPost("http://localhost:8080/some-other-path/metadata.xml");
		System.out.println(IOUtils.toString(response1.getEntity().getContent()));

		// Comment these 2 lines and execute the test. Then uncomment them and execute it again
		HttpResponse response2 = simulator.performCorrectPost("http://localhost:8080/some-path/metadata.xml");
		System.out.println(IOUtils.toString(response2.getEntity().getContent()));
		
		// Verify the actual requests
		verify(postRequestedFor(
				urlEqualTo("/some-path/metadata.xml"))
				.withHeader("Content-Type", equalTo("json"))
				);
		
		// Finish doing stuff
		wireMockServer.stop();
	}
}
