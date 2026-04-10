package com.ootd.fitme.infrastructure.external.mail;

import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailSender;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

public class SmtpTemporaryPasswordMailSender implements TemporaryPasswordMailSender {
    private final JavaMailSender javaMailSender;
    private final String from;
    private final String subject;

    public SmtpTemporaryPasswordMailSender(JavaMailSender javaMailSender, String from, String subject) {
        this.javaMailSender = javaMailSender;
        this.from = from;
        this.subject = subject;
    }

    @Override
    public void sendTemporaryPassword(String toEmail, String temporaryPassword, Duration ttl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(buildBody(temporaryPassword, ttl));

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new IllegalStateException("temporary password mail and failed", e);
        }
    }

    private String buildBody(String temporaryPassword, Duration ttl) {
        return """
                FitMe 임시 비밀번호가 발급되었습니다.
                
                임시 비밀번호: %s
                유효 시간: 발급 후 %d분
                
                로그인 후 반드시 비밀번호를 변경해 주세요.
                """.formatted(temporaryPassword, ttl.toMinutes());
    }
}
