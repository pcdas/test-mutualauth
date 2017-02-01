package mutualauth;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
@TestPropertySource("/my-test.properties")
public class MyServiceTest3 {

	private final Log logger = LogFactory.getLog(getClass());
	@Value("${server.ssl.hostname}")
	private String hostname;
	@Value("${server.ssl.port}")
    private int port;

    @Value("${client.ssl.key-store}")
    private String keyStore;
    @Value("${client.ssl.key-store-password}")
    private char[] keyStorePassword;

    @Value("${client.ssl.trust-store}")
    private String trustStore;
    @Value("${client.ssl.trust-store-password}")
    private char[] trustStorePassword;

    private SSLContext ctx;

    private URL base;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("https://" + hostname + ":" + port + "/index");

        ctx = SSLContext.getInstance("TLS");
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(keyStore), keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
        		KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyStorePassword);

        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream(trustStore), trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), SecureRandom.getInstanceStrong());
    }

	@Test
	public void getIndexUsingX509Cert() throws GeneralSecurityException {
		CloseableHttpClient httpClient =
			HttpClients
				.custom()
				.setSSLContext(ctx)
				.setSSLHostnameVerifier(this::verifyHost).build();
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

	private boolean verifyHost(String hostname, javax.net.ssl.SSLSession sess) {
		try {
			if (logger.isDebugEnabled()) {
				Certificate[] certs = sess.getPeerCertificates();
				String cert = String.format("verifyHost certificate: type=%s [%s]",
						certs[0].getType(), certs[0]);
				logger.info(cert);
				String s = String.format("verifyHost hostname=%s, peerHost=%s, peerPort=%d",
						hostname, sess.getPeerHost(), sess.getPeerPort());
				logger.info(s);
			}
			return hostname.equalsIgnoreCase(extractCommonName(sess.getPeerPrincipal().toString()));
		} catch (SSLPeerUnverifiedException e) {
			logger.info("Hostname verification failed.", e);
			return false;
		}
	}

	private String extractCommonName(String certPrincipal) {
		logger.info(String.format("verifyHost peerPrincipal=[%s]", certPrincipal));
		Matcher m = Pattern.compile("CN=(.*?),").matcher(certPrincipal);
		return m.find() ? m.group(1) : "";
	}

}
