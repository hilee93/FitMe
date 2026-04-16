package com.ootd.fitme.domain.user.service.temppassword;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Primary
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisTemporaryPasswordMailRetryQueue implements TemporaryPasswordMailRetryQueue {
    private static final String KEY = "fitme:auth:temp-password:mail-retry:zset";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisTemporaryPasswordMailRetryQueue(StringRedisTemplate redisTemplate,
                                                ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void enqueue(TemporaryPasswordMailRetryCommand command) {
        String member = toMember(command);
        double score = command.nextAttemptAt().toEpochMilli();

        redisTemplate.opsForZSet().add(KEY, member, score);
    }

    @Override
    public List<TemporaryPasswordMailRetryCommand> pollDue(Instant now, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        long nowEpochMs = now.toEpochMilli();

        List<TemporaryPasswordMailRetryCommand> due = new ArrayList<>(limit);

        for (int i = 0; i < limit; i++) {
            ZSetOperations.TypedTuple<String> tuple = popOne();

            if (tuple == null || tuple.getValue() == null) {
                break;
            }

            Double score = tuple.getScore();
            String member = tuple.getValue();
            long scoreEpochMs = score != null ? score.longValue() : Long.MAX_VALUE;

            // 아직 실행 시간이 안됐으면 다시 넣고 종료
            if (scoreEpochMs > nowEpochMs) {
                redisTemplate.opsForZSet().add(KEY, member, scoreEpochMs);
                break;
            }

            TemporaryPasswordMailRetryCommand command = fromMember(member);
            if (command != null) {
                due.add(command);
            }
        }
        return due;
    }

    @Override
    public int size() {
        Long size = redisTemplate.opsForZSet().size(KEY);
        if (size == null) {
            return 0;
        }
        return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : size.intValue();
    }

    private ZSetOperations.TypedTuple<String> popOne() {
        Set<ZSetOperations.TypedTuple<String>> popped = redisTemplate.opsForZSet().popMin(KEY, 1);
        if (popped == null || popped.isEmpty()) {
            return null;
        }
        return popped.iterator().next();
    }

    private String toMember(TemporaryPasswordMailRetryCommand command) {
        try {
            String payload = objectMapper.writeValueAsString(command);
            return UUID.randomUUID() + "|" + payload;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("retry queue 직렬화 실패", e);
        }
    }

    private TemporaryPasswordMailRetryCommand fromMember(String member) {
        int sep = member.indexOf('|');

        if (sep < 0 || sep + 1 >= member.length()) {
            return null;
        }

        String payload = member.substring(sep + 1);

        try {
            return objectMapper.readValue(payload, TemporaryPasswordMailRetryCommand.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
