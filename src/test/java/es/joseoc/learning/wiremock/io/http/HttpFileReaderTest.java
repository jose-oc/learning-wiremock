package es.joseoc.learning.wiremock.io.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.Fault.MALFORMED_RESPONSE_CHUNK;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import es.joseoc.learning.wiremock.io.http.exceptions.HttpClientException;
import es.joseoc.learning.wiremock.io.http.exceptions.HttpContentNotFound;
import es.joseoc.learning.wiremock.io.http.exceptions.HttpServerException;

@RunWith(HierarchicalContextRunner.class)
public class HttpFileReaderTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(new WireMockConfiguration().dynamicPort());
		
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	public class WhenSuccess {
		
		@Test
		public void testGetFileSize() throws Exception {
			String content = "<response>Some content</response>";
			stubFor(head(urlEqualTo("/some-path/metadata.xml"))
					.willReturn(aResponse()
							.withStatus(200)
							.withHeader("Content-Type", "text/xml")
							.withHeader("Content-Length", Objects.toString(content.getBytes().length))
							.withBody(content)));
			
			long actual = new HttpFileReader().getFileSize(URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			
			assertEquals(content.length(), actual);
		}

		@Test
		public void testReadFileAsString() throws Exception {
			String content = "<response>Some content</response>";
			stub200(content, "text/xml");
			
			String actual = new HttpFileReader().readFileAsString(URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));

			assertEquals(content, actual);
			assertEquals(content.getBytes().length, actual.length());
		}

		@Test
		public void testListFilesWhenNoHref() throws Exception {
			stub200("<response>Some content</response>", "text/xml");
			
			Collection<String> actual = new HttpFileReader().listFiles(
					URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			
			assertEquals(0, actual.size());
		}

		@Test
		public void testListFiles() throws Exception {
			stub200("<p>bla</p><a href=\"link\">link text</a><p>bla</p><a href=\"link2\">link text2</a>", "text/html");
			
			Collection<String> actual = new HttpFileReader().listFiles(
					URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			
			assertEquals(2, actual.size());
		}

		private void stub200(String content, String contentType) {
			stubFor(get(urlEqualTo("/some-path/metadata.xml"))
					.willReturn(aResponse()
							.withStatus(200)
							.withHeader("Content-Type", contentType)
							.withHeader("Content-Length", Objects.toString(content.getBytes().length))
							.withBody(content)));
		}

	}
	
	public class WhenNotFoundError {
	
		@Test
		public void testReadFileAsString() throws Exception {
			String content = "<html><head><title>404NotFound</title></head><body><h1>404NotFound</h1><ul><li>Code:NoSuchKey</li><li>Message:Thespecifiedkeydoesnotexist.</li><li>Key:publish/metadata_identity_file_1asset.json-kk</li><li>RequestId:CCAF5B8D</li><li>HostId:RzFGNoh0nnPFPNgf+c=</li></ul><hr/></body></html>";
			stub404(content);
			
			Exception actual = null;
			try {
				new HttpFileReader().readFileAsString(URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			} catch (Exception e) {
				actual = e;
			}
			
			assertTrue(actual instanceof HttpContentNotFound);
			assertThat(actual.getMessage(), containsString("Content not found in the source http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml returning the response: HttpResponseProxy{HTTP/1.1 404 Not Found"));
		}
	
		@Test
		public void testListFiles() throws Exception {
			String content = "<html><head><title>404NotFound</title></head><body><h1>404NotFound</h1><ul><li>Code:NoSuchKey</li><li>Message:Thespecifiedkeydoesnotexist.</li><li>Key:publish/metadata_identity_file_1asset.json-kk</li><li>RequestId:CCAF5B8D</li><li>HostId:RzFGNoh0nnPFPNgf+c=</li></ul><hr/></body></html>";
			stub404(content);
			
			Exception actual = null;
			try {
				new HttpFileReader().listFiles(URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			} catch (Exception e) {
				actual = e;
			}
			
			assertEquals("Error parsing HTML. URL http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml. Status code: 404 ", actual.getMessage());
		}
			
		private void stub404(String content) {
			stubFor(get(urlEqualTo("/some-path/metadata.xml"))
					.willReturn(aResponse()
							.withStatus(404)
							.withHeader("Content-Type", "text/html")
							.withHeader("Content-Length", Objects.toString(content.getBytes().length))
							.withBody(content)));
		}
	
	}

	public class WhenClientError {
		
		@Test
		public void testReadFileAsString() throws Exception {
			String content = "<html><head><title>Apache Tomcat/7.0.54 - Error report</title><style><!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} H2 {font-family:Tahoma,Arial,sans-serif;color:white;backgramily:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} B {font-family:Tahoma,Arial,sans-serif;color:white;background-ns-serif;background:white;color:black;font-size:12px;}A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> </head><body><h1>HTTP Status 405 - Method Not Allowed</h1><HR size=\"1\" noshade=\"noshade\"><p><b>tyethod Not Allowed</u></p><p><b>description</b> <u>The specified HTTP method is not allowed for the requested resource.</u></p><HR size=\"1\" noshade=\"noshade\"><h3>Apache Tomcat/7.0.54</h3></body></html>";
			stub405(content);
			
			Exception actual = null;
			try {
				new HttpFileReader().readFileAsString(URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			} catch (Exception e) {
				actual = e;
			}
			
			assertTrue(actual instanceof HttpClientException);
			assertThat(actual.getMessage(), containsString("Client error. Source http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml returning the response: HttpResponseProxy{HTTP/1.1 405 Method Not Allowed"));
		}
	
		@Test
		public void testListFiles() throws Exception {
			String content = "<html><head><title>Apache Tomcat/7.0.54 - Error report</title><style><!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} H2 {font-family:Tahoma,Arial,sans-serif;color:white;backgramily:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} B {font-family:Tahoma,Arial,sans-serif;color:white;background-ns-serif;background:white;color:black;font-size:12px;}A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> </head><body><h1>HTTP Status 405 - Method Not Allowed</h1><HR size=\"1\" noshade=\"noshade\"><p><b>tyethod Not Allowed</u></p><p><b>description</b> <u>The specified HTTP method is not allowed for the requested resource.</u></p><HR size=\"1\" noshade=\"noshade\"><h3>Apache Tomcat/7.0.54</h3></body></html>";
			stub405(content);
			
			try {
				new HttpFileReader().listFiles(URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			} catch (Exception e) {
				assertEquals("Error parsing HTML. URL http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml. Status code: 405 ", e.getMessage());
			}
		}
		
		private void stub405(String content) {
			stubFor(get(urlEqualTo("/some-path/metadata.xml"))
					.willReturn(aResponse()
							.withStatus(405)
							.withStatusMessage("Method Not Allowed")
							.withHeader("Content-Type", "text/html")
							.withHeader("Content-Length", Objects.toString(content.getBytes().length))
							.withHeader("Allow", "POST,PUT")
							.withBody(content)));
		}
	
	}
	
	public class WhenServerError {
	
		@Test
		public void testReadFileAsString() throws Exception {
			String content = "<html><head><title>404NotFound</title></head><body><h1>404NotFound</h1><ul><li>Code:NoSuchKey</li><li>Message:Thespecifiedkeydoesnotexist.</li><li>Key:publish/metadata_identity_file_1asset.json-kk</li><li>RequestId:CCAF5B8D</li><li>HostId:RzFGNoh0nnPFPNgf+c=</li></ul><hr/></body></html>";
			stub500(content);
			
			Exception actual = null;
			try {
				new HttpFileReader().readFileAsString(URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			} catch (Exception e) {
				actual = e;
			}
			
			assertTrue(actual instanceof HttpServerException);
			assertThat(actual.getMessage(), containsString("Error in the server. Source http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml returning the response: HttpResponseProxy{HTTP/1.1 500 Server"));
		}
	
		@Test
		public void testListFiles() throws Exception {
			String content = "some response";
			stub500(content);
			
			try {
				new HttpFileReader().listFiles(URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml"));
			} catch (Exception e) {
				assertEquals("Error parsing HTML. URL http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml. Status code: 500 ", e.getMessage());
			}
		}
		
		private void stub500(String content) {
			stubFor(get(urlEqualTo("/some-path/metadata.xml"))
					.willReturn(aResponse()
							.withStatus(500)
							.withHeader("Content-Type", "text/html")
							.withHeader("Content-Length", Objects.toString(content.getBytes().length))
							.withBody(content)));
		}

	}
	
	public class WhenFaults {
		
		@Test(expected=HttpServerException.class)
		public void testReadWhenRandomResponse() throws Exception {
			stubFor(get(urlEqualTo("/some-path/metadata.xml"))
					.willReturn(aResponse()
							.withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
			
			URI uri = URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml");
			new HttpFileReader().readFileAsString(uri);
		}

		@Test(expected=org.apache.http.MalformedChunkCodingException.class)
		public void testReadWhenCorruptedResponse() throws Exception {
			stubFor(get(urlEqualTo("/some-path/metadata.xml"))
					.willReturn(aResponse()
							.withFault(MALFORMED_RESPONSE_CHUNK)));
			
			URI uri = URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml");
			new HttpFileReader().readFileAsString(uri);
		}

		@Test(expected=HttpServerException.class)
		public void testReadWhenNoResponse() throws Exception {
			stubFor(get(urlEqualTo("/some-path/metadata.xml"))
					.willReturn(aResponse()
							.withFault(Fault.EMPTY_RESPONSE)));

			URI uri = URI.create("http://usuario:password@localhost:" + wireMockRule.port() + "/some-path/metadata.xml");
			new HttpFileReader().readFileAsString(uri);
		}
		
	}
	
}
