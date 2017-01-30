package mutualauth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;

//@RestController combines @Controller and @ResponseBody
@Controller
@ResponseBody
public class MyServiceController {
	@RequestMapping("/index")
	@PreAuthorize("hasRole('CERT_USER')")
	String index() {
		return "Hello from MyService authenticated with x509 certificate...\n";
	}

	@RequestMapping("/basic-auth/index")
	@PreAuthorize("hasRole('USER')")
	String basicAuthIndex() {
		return "Hello from MyService using basic authentication...\n";
	}

	@RequestMapping("/unauth/index")
	String unauthIndex() {
		return "Unauthenticated hello from MyService...\n";
	}

//	@RequestMapping("/shutdown")
//	String shutDown() {
//		System.exit(0);
//		return "Exiting...\n";
//	}


}
