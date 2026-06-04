package com.clinic.clinic.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailService {
    //private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String username, String subject, EmailTemplateName templateName,
                          String confirmationurl, String activationCode) throws MessagingException {
        System.out.println("Sending email to " + to);
        System.out.println("Username: " + username);
        System.out.println("Subject: " + subject);
        System.out.println("ConfirmationURL: " + confirmationurl);
        System.out.println("ActivationCode: " + activationCode);
        String template;
        if(templateName == null){
            template = "email_confirmation";
        }else{
            template = templateName.getTemplateName();
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED, StandardCharsets.UTF_8.name());
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationurl);
        properties.put("activation_code", activationCode);
        Context context = new Context();
        context.setVariables(properties);
        mimeMessageHelper.setFrom("contact@clinicIVCA.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        String templateText = templateEngine.process(template, context);
        mimeMessageHelper.setText(templateText, true);
        mailSender.send(mimeMessage);
    }
}
