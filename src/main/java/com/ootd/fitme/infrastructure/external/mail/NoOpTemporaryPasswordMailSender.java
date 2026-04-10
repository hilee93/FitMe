package com.ootd.fitme.infrastructure.external.mail;

import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailSender;

import java.time.Duration;

public class NoOpTemporaryPasswordMailSender implements TemporaryPasswordMailSender {
    @Override
    public void sendTemporaryPassword(String toEmail, String temporaryPassword, Duration ttl) {
        // local/test 기본 no-op
    }
}
