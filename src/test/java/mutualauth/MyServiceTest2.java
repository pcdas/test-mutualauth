package mutualauth;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.security.GeneralSecurityException;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/my-test.properties")
public class MyServiceTest2 {

    @LocalServerPort
    private int port;

    @Value("${client.ssl.trust-store}")
    private String trustStore;
    @Value("${client.ssl.trust-store-password}")
    private String trustStorePassword;
    
    private URL base;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("https://localhost:" + port + "/unauth/index");
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

	@Test
	public void getUnauthIndex() throws GeneralSecurityException {
		CloseableHttpClient httpClient =
			HttpClients
				.custom()
				.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		String urlOverHttps = base.toString();
		ResponseEntity<String> response = new RestTemplate(requestFactory)
				.exchange(urlOverHttps, HttpMethod.GET, null, String.class);
		assertThat(response.getStatusCode().value(), equalTo(200));
		assertThat(response.getBody(),
				equalTo("Unauthenticated hello from MyService...\n"));
	}

}