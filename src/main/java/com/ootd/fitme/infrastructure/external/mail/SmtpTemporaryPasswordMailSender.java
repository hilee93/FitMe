package com.ootd.fitme.infrastructure.external.mail;

import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailSender;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class SmtpTemporaryPasswordMailSender implements TemporaryPasswordMailSender {
    private static final String DEFAULT_BODY_TEMPLATE = """
                FitMe 임시 비밀번호가 발급되었습니다.
                
                임시 비밀번호: {tempPassword}
                유효 시간: 발급 후 {ttlMinutes}분
                
                로그인 후 반드시 비밀번호를 변경해 주세요.
                """;

    private final JavaMailSender javaMailSender;
    private final MeterRegistry meterRegistry;
    private final String from;
    private final String subject;
    private final String bodyTemplate;

    public SmtpTemporaryPasswordMailSender(
            JavaMailSender javaMailSender,
            MeterRegistry meterRegistry,
            String from,
            String subject,
            String bodyTemplate
    ) {
        this.javaMailSender = javaMailSender;
        this.meterRegistry = meterRegistry;
        this.from = from;
        this.subject = subject;
        this.bodyTemplate = bodyTemplate;
    }

    @Override
    @Retryable(
            retryFor = MailException.class,
            maxAttemptsExpression = "${fitme.user.temp-password.mail.retry.max-attempts:2}",
            backoff = @Backoff(delayExpression = "${fitme.user.temp-password.mail.retry.backoff-ms:500}")
    )
    public void sendTemporaryPassword(String toEmail, String temporaryPassword, Duration ttl) {
        long startedAtNanos = System.nanoTime();
        meterRegistry.counter("fitme.mail.temp-password.send.attempt", "provider", "smtp").increment();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(buildBody(temporaryPassword, ttl));

        try {
            javaMailSender.send(message);
            meterRegistry.counter("fitme.mail.temp-password.send.success", "provider", "smtp").increment();
            meterRegistry.timer("fitme.mail.temp-password.send.duration", "provider", "smtp")
                    .record(System.nanoTime() - startedAtNanos, TimeUnit.NANOSECONDS);
        } catch (MailException e) {
            meterRegistry.counter("fitme.mail.temp-password.send.fail", "provider", "smtp").increment();
            throw e;
        }
    }

    @Recover
    public void recover(MailException e, String toEmail, String temporaryPassword, Duration ttl) {
        meterRegistry.counter("fitme.mail.temp-password.send.fail", "provider", "smtp").increment();
        throw new IllegalStateException("temporary password mail send failed", e);
    }

    private String buildBody(String temporaryPassword, Duration ttl) {
        String template = bodyTemplate == null || bodyTemplate.isBlank()
                ? DEFAULT_BODY_TEMPLATE : bodyTemplate;
        return template
                .replace("{tempPassword}", temporaryPassword)
                .replace("{ttlMinutes}", String.valueOf(ttl.toMinutes()));
    }
}
