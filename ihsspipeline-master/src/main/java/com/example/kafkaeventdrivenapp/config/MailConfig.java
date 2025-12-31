package com.example.kafkaeventdrivenapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${MAIL_HOST:}")
    private String mailHost;

    @Value("${MAIL_PORT:1025}")
    private int mailPort;

    @Value("${MAIL_USERNAME:}")
    private String mailUsername;

    @Value("${MAIL_PASSWORD:}")
    private String mailPassword;

    @Bean
    public JavaMailSender javaMailSender() {
        System.out.println("🔧 MailConfig: Initializing JavaMailSender");
        System.out.println("📧 Mail Host: " + mailHost);
        System.out.println("📧 Mail Port: " + mailPort);
        System.out.println("📧 Mail Username: " + mailUsername);
        System.out.println("📧 Mail Password: " + (mailPassword != null && !mailPassword.isEmpty() ? "[SET]" : "[EMPTY]"));
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        
        // Only enable authentication if username and password are provided
        if (mailUsername != null && !mailUsername.trim().isEmpty() && 
            mailPassword != null && !mailPassword.trim().isEmpty()) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            System.out.println("🔧 MailConfig: Authentication ENABLED");
        } else {
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.starttls.enable", "false");
            System.out.println("🔧 MailConfig: Authentication DISABLED (no credentials)");
        }
        
        props.put("mail.debug", "false");
        System.out.println("🔧 MailConfig: JavaMailSender configured successfully");

        return mailSender;
    }
}
