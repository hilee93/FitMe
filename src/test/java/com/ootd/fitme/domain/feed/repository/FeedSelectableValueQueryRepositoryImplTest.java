package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class, FeedSelectableValueQueryRepositoryImpl.class})
class FeedSelectableValueQueryRepositoryImplTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private FeedSelectableValueQueryRepository feedSelectableValueQueryRepository;

    @Test
    void findFeedSelectableValuesByAttributeIds_returns_all_values_of_attribute() {
        // given
        Attribute size = em.persist(Attribute.create("사이즈"));
        em.persist(SelectableValue.create("S", size));
        em.persist(SelectableValue.create("M", size));
        em.persist(SelectableValue.create("L", size));
        em.persist(SelectableValue.create("FREE", size));

        em.flush();
        em.clear();

        // when
        Map<UUID, List<String>> result =
                feedSelectableValueQueryRepository.findFeedSelectableValuesByAttributeIds(List.of(size.getId()));

        // then
        assertThat(result).containsKey(size.getId());
        assertThat(result.get(size.getId()))
                .containsExactlyInAnyOrder("S", "M", "L", "FREE");
    }
}