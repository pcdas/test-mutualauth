package mutualauth;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

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
public class MyServiceTest3 {

    @LocalServerPort
    private int port;

    @Value("${client.ssl.key-store}")
    private String keyStore;
    @Value("${client.ssl.key-store-password}")
    private String keyStorePassword;
    private SSLContext ctx;

    private URL base;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("https://localhost:" + port + "/index");

        ctx = SSLContext.getInstance("TLS");
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(keyStore), keyStorePassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
        		KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyStorePassword.toCharArray());
        ctx.init(kmf.getKeyManagers(), null, SecureRandom.getInstanceStrong());
    }

	@Test
	public void getIndexUsingX509Cert() throws GeneralSecurityException {
		CloseableHttpClient httpClient =
			HttpClients
				.custom()
				.setSSLContext(ctx)
				.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
		HttpComponentsClientHttpRequestFactory requestFactory =
				new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		String urlOverHttps = base.toString();
		ResponseEntity<String> response = new RestTemplate(requestFactory)
				.exchange(urlOverHttps, HttpMethod.GET, null, String.class);
		assertThat(response.getStatusCode().value(), equalTo(200));
		assertThat(response.getBody(),
				equalTo("Hello from MyService authenticated with x509 certificate...\n"));
	}

}