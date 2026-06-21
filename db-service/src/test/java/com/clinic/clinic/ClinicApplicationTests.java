package com.clinic.clinic;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"application.security.jwt.secret-key=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
		"application.security.jwt.expiration=86400000",
		"application.security.jwt.refresh-token.expiration=604800000",
		"application.mailing.frontend.activation-url=http://localhost:4200/activate-account"
})
class ClinicApplicationTests {

	@MockitoBean
	private JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}
}
