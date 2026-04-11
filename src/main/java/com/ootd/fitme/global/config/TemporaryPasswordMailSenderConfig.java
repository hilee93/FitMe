package com.ootd.fitme.global.config;

import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailSender;
import com.ootd.fitme.infrastructure.external.mail.NoOpTemporaryPasswordMailSender;
import com.ootd.fitme.infrastructure.external.mail.SmtpTemporaryPasswordMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class TemporaryPasswordMailSenderConfig {
    @Bean
    public TemporaryPasswordMailSender temporaryPasswordMailSender(
            @Value("${fitme.user.temp-password.mail.enabled:false}") boolean enabled,
            @Value("${fitme.user.temp-password.mail.from}") String from,
            @Value("${fitme.user.temp-password.mail.subject:[FitMe] 임시 비밀번호 안내}") String subject,
            ObjectProvider<JavaMailSender> javaMailSenderProvider
    ) {
        JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();

        if (enabled && javaMailSender != null) {
            return new SmtpTemporaryPasswordMailSender(javaMailSender, from, subject);
        }
        return new NoOpTemporaryPasswordMailSender();
    }
}
