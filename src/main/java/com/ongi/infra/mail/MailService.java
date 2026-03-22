package com.ongi.infra.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${ongi.mail.from}")
    private String fromEmail;

    @Value("${ongi.mail.from-name}")
    private String fromName;

    @Async
    public void sendVerificationEmail(String toEmail, String toName, String verifyToken) {
        Context context = new Context();
        context.setVariable("name", toName != null ? toName : "구독자");
        context.setVariable("verifyUrl",
                "http://localhost:3000/verify?token=" + verifyToken);

        String html = templateEngine.process("email/verify-email", context);
        send(toEmail, "[온기] 이메일 인증을 완료해주세요", html);
    }

    public void sendEmailWithContent(String toEmail, String subject, String html) {
        send(toEmail, subject, html);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}
