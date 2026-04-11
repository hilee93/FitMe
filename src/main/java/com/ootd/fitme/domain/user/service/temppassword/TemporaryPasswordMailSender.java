package com.ootd.fitme.domain.user.service.temppassword;

import java.time.Duration;

public interface TemporaryPasswordMailSender {
    void sendTemporaryPassword(String toEmail, String temporaryPassword, Duration ttl);
}
