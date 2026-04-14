package com.ootd.fitme.domain.user.scheduler;

import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailRetryCommand;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailRetryQueue;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailSender;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemporaryPasswordMailRetryScheduler {
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_DELAY_MS = 30_000L;
    private static final int BATCH_SIZE = 20;
    private static final long POLL_INTERVAL_MS = 5_000L;

    private final TemporaryPasswordMailSender temporaryPasswordMailSender;
    private final TemporaryPasswordMailRetryQueue retryQueue;
    private final TemporaryPasswordStore temporaryPasswordStore;

    @Scheduled(fixedDelay = POLL_INTERVAL_MS)
    public void processRetryQueue() {
        List<TemporaryPasswordMailRetryCommand> due = retryQueue.pollDue(Instant.now(), BATCH_SIZE);
        if (due.isEmpty()) {
            return;
        }

        for (TemporaryPasswordMailRetryCommand command : due) {
            processOne(command);
        }
    }

    private void processOne(TemporaryPasswordMailRetryCommand command) {
        try {
            temporaryPasswordMailSender.sendTemporaryPassword(
                    command.toEmail(),
                    command.temporaryPassword(),
                    command.ttl()
            );
            log.info("[TEMP_PASSWORD_MAIL][RETRY_SUCCESS] userEmail={}, attempt={}",
                    maskEmail(command.toEmail()), command.attempt());
        } catch (RuntimeException e) {
            if (command.attempt() >= MAX_ATTEMPTS) {
                temporaryPasswordStore.delete(command.userId());
                log.error("[TEMP_PASSWORD_MAIL][RETRY_EXHAUSTED] userEmail={}, attempt={}, tempPasswordDeleted=true",
                        maskEmail(command.toEmail()), command.attempt(), e);
                return;
            }

            TemporaryPasswordMailRetryCommand next = command.withNextAttempt(nextAttemptAt(command.attempt() + 1));
            retryQueue.enqueue(next);

            log.warn("[TEMP_PASSWORD_MAIL][RETRY_REENQUEUE] userEmail={}, nextAttempt={}, queueSize={}",
                    maskEmail(command.toEmail()), next.attempt(), retryQueue.size());
        }
    }

    private Instant nextAttemptAt(int nextAttempt) {
        long delay = BASE_DELAY_MS;
        for (int i = 1; i < nextAttempt; i++) {
            if (delay > Long.MAX_VALUE / 2) {
                delay = Long.MAX_VALUE;
                break;
            }
            delay *= 2;
        }
        return Instant.now().plusMillis(delay);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }

        int at = email.indexOf('@');
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
