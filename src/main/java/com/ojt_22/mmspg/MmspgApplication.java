package com.ojt_22.mmspg;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MmspgApplication {

	public static void main(String[] args) {
		SpringApplication.run(MmspgApplication.class, args);
	}

	@Bean
	@ConditionalOnMissingBean(JavaMailSender.class)
	public JavaMailSender javaMailSender(
			@Value("${spring.mail.host:smtp.gmail.com}") String host,
			@Value("${spring.mail.port:587}") int port,
			@Value("${spring.mail.username:winwin555620@gmail.com}") String username,
			@Value("${spring.mail.password:ktokbzhnarlbgsan}") String password) {

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(username);
		mailSender.setPassword(password);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

		return mailSender;
	}

}

