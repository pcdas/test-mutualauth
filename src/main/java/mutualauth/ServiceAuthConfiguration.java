package mutualauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

@EnableWebSecurity
public class ServiceAuthConfiguration {
	@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // enable in memory based authentication with a user named "user" and "admin"
        auth.inMemoryAuthentication()
        .withUser("user").password("password").roles("USER")
        .and()
        .withUser("admin").password("password").roles("USER", "ADMIN");
    }

    @Configuration
    @Order(1)
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class BasicAuthSecurityConfigurer extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.antMatcher("/basic-auth/index")
					.authorizeRequests()
					.anyRequest()
					.authenticated().and()
					.httpBasic();
		}
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
	public static class CertBasedAuthSecurityConfigurer extends
			WebSecurityConfigurerAdapter {
		@Value("${server.ssl.client.regex}")
		private String COMMON_NAME_EXTRACTOR;

		@Autowired
		private UserDetailsService userDetailsService;

		@Override
		public void configure(WebSecurity web) throws Exception {
			// Spring Security should completely ignore URLs starting with
			// /unauth/
			web.ignoring().antMatchers("/unauth/**");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable()
					.sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					.and()
					.antMatcher("/index")
					.authorizeRequests()
					.anyRequest()
					.authenticated()
					.and().x509()
					.subjectPrincipalRegex(COMMON_NAME_EXTRACTOR)
					.userDetailsService(userDetailsService);
		}

	}
    
}
