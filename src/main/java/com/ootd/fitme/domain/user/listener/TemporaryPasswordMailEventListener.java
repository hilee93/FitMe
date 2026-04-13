package com.ootd.fitme.domain.user.listener;

import com.ootd.fitme.domain.user.event.TemporaryPasswordMailRequestedEvent;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailRetryCommand;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailRetryQueue;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemporaryPasswordMailEventListener {
    private static final long BASE_DELAY_MS = 30_000L;

    private final TemporaryPasswordMailSender temporaryPasswordMailSender;
    private final TemporaryPasswordMailRetryQueue retryQueue;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTemporaryPasswordMailRequested(TemporaryPasswordMailRequestedEvent event) {
        try {
            temporaryPasswordMailSender.sendTemporaryPassword(
                    event.toEmail(),
                    event.temporaryPassword(),
                    event.ttl()
            );
            log.info("[TEMP_PASSWORD_MAIL][SEND_SUCCESS] userEmail={}", maskEmail(event.toEmail()));
        } catch (RuntimeException e) {
            retryQueue.enqueue(new TemporaryPasswordMailRetryCommand(
                    event.userId(),
                    event.toEmail(),
                    event.temporaryPassword(),
                    event.ttl(),
                    1,
                    Instant.now().plusMillis(BASE_DELAY_MS)
            ));
            log.warn("[TEMP_PASSWORD_MAIL][ENQUEUE_RETRY] userEmail={}, queueSize={}",
                    maskEmail(event.toEmail()), retryQueue.size(), e);
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }

        int at =  email.indexOf('@');
        if (at <= 0) {
            return "***";
        }

        String local = email.substring(0, at);
        String domain = email.substring(at);

        if (local.length() == 1) {
            return local.charAt(0) + "***" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }
}
