package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.clothesattribute.repository.ClothesAttributeRepository;
import com.ootd.fitme.domain.clothesattributeselectablevalue.repository.ClothesAttributeSelectableValueRepository;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.feed.repository.FeedRepository;
import com.ootd.fitme.domain.feedclothes.repository.FeedClothesRepository;
import com.ootd.fitme.domain.feedlike.repository.FeedLikeRepository;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.selectablevalue.repository.SelectableValueRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class FeedServiceConcurrencyTest {

    @Autowired
    private FeedService feedService;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private FeedLikeRepository feedLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private WeatherForecastRepository weatherForecastRepository;

    @Autowired
    private ClothesRepository clothesRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private SelectableValueRepository selectableValueRepository;

    @Autowired
    private ClothesAttributeRepository clothesAttributeRepository;

    @Autowired
    private ClothesAttributeSelectableValueRepository clothesAttributeSelectableValueRepository;

    @Autowired
    private FeedClothesRepository feedClothesRepository;


    @Autowired
    private EntityManager em;


    @AfterEach
    void cleanup() {
        feedLikeRepository.deleteAllInBatch();
        feedClothesRepository.deleteAllInBatch();
        // commentRepository.deleteAllInBatch();
        // notificationRepository.deleteAllInBatch();
        // followRepository.deleteAllInBatch();
        // userWeatherNotificationRepository.deleteAllInBatch();
        // userActiveLocationRepository.deleteAllInBatch();

        feedRepository.deleteAllInBatch();

        clothesAttributeSelectableValueRepository.deleteAllInBatch();
        clothesAttributeRepository.deleteAllInBatch();
        selectableValueRepository.deleteAllInBatch();

        clothesRepository.deleteAllInBatch();
        weatherForecastRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();

        attributeRepository.deleteAllInBatch();
        regionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
//    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) // NOTE: 롤백안된 데이터 실제 Sql drop table 읽도록할때 추가
    @DisplayName("[Concurrency] 서로 다른 사용자가 같은 피드에 동시에 좋아요를 누르면 likeCount가 정확히 증가한다")
    void likeFeed_success_when_concurrent_like() throws InterruptedException, ExecutionException {

        //given
        FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
        Feed feed = feedFixture.feed();
        User author = feedFixture.user();
        User liker1 = userRepository.save(
                User.create("liker-" + UUID.randomUUID() + "@test.com", "password")
        );
        User liker2 = userRepository.save(
                User.create("liker-" + UUID.randomUUID() + "@test.com", "password")
        );

        UUID feedId = feed.getId();

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Future<Object>> futures = List.of(liker1.getId(), liker2.getId()).stream()
                .map(likerId -> executorService.submit(() -> {
                    readyLatch.countDown();
                    startLatch.await();

                    try {
                        feedService.likeFeed(feedId, likerId);
                    } finally {
                        doneLatch.countDown();
                    }
                    return null;
                }))
                .toList();

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        for (Future<?> future : futures) {
            future.get(); // 여기서 작업 스레드 예외를 메인 스레드가 알 수 있음
        }

        em.clear();

        Feed reloadFeed = feedRepository.findById(feedId).orElseThrow();
        assertThat(reloadFeed.getLikeCount()).isEqualTo(2);

        assertThat(feedLikeRepository.existsByFeedIdAndUserId(feedId, liker1.getId())).isTrue();
        assertThat(feedLikeRepository.existsByFeedIdAndUserId(feedId, liker2.getId())).isTrue();

        executorService.shutdown();

    }
}
