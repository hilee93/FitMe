package com.ootd.fitme.domain.user.service.temppassword;

import java.time.Instant;
import java.util.List;

public interface TemporaryPasswordMailRetryQueue {
    void enqueue(TemporaryPasswordMailRetryCommand command);
    List<TemporaryPasswordMailRetryCommand> pollDue(Instant now, int limit);
    int size();
}
