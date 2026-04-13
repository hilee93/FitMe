package com.ootd.fitme.domain.user.service.temppassword;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InMemoryTemporaryPasswordMailRetryQueue implements TemporaryPasswordMailRetryQueue {
    private final PriorityQueue<TemporaryPasswordMailRetryCommand> queue =
            new PriorityQueue<>(Comparator.comparing(TemporaryPasswordMailRetryCommand::nextAttemptAt));
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void enqueue(TemporaryPasswordMailRetryCommand command) {
        lock.lock();
        try {
            queue.offer(command);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<TemporaryPasswordMailRetryCommand> pollDue(Instant now, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        lock.lock();
        try {
            List<TemporaryPasswordMailRetryCommand> due = new ArrayList<>();
            while (due.size() < limit && !queue.isEmpty()) {
                TemporaryPasswordMailRetryCommand head = queue.peek();
                if (head.nextAttemptAt().isAfter(now)) {
                    break;
                }
                due.add(queue.poll());
            }
            return due;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
