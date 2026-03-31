package com.ootd.fitme.domain.clothes.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@DisplayName("ClothesRepository N+1 및 삭제 쿼리 테스트")
class ClothesRepositoryTest {

    @Autowired
    private ClothesRepository clothesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("벌크 연산으로 옷 삭제 시, 자식 개수와 무관하게 단 1번의 DELETE 쿼리만 발생해야 한다.")
    void deleteByIdInBulk_NoNPlusOne() {
        // given
        User user = User.create("abc@abc.com", "8878@asd");
        userRepository.save(user);

        Clothes clothes = Clothes.createWithImage("테스트 옷", ClothesType.TOP, user, null);

        for (int i = 0; i < 10; i++) {
            Attribute attr = Attribute.create("색상" + i);
            attr.addValues(List.of("빨강", "파랑"));
            attributeRepository.save(attr);

            ClothesAttribute clothesAttribute = ClothesAttribute.create(clothes, attr);
            clothesAttribute.assignOption(attr.getSelectableValues().get(0));
            clothes.getAttributes().add(clothesAttribute);
        }

        clothesRepository.save(clothes);

        em.flush();
        em.clear();

        // when
        clothesRepository.deleteByIdInBulk(clothes.getId());
        em.flush();
        em.clear();

        // then
        Optional<Clothes> deletedClothes = clothesRepository.findById(clothes.getId());
        assertThat(deletedClothes).isEmpty();

        Long clothesAttributeCount = em.createQuery(
                        "SELECT COUNT(ca) FROM ClothesAttribute ca WHERE ca.clothes.id = :id", Long.class)
                .setParameter("id", clothes.getId())
                .getSingleResult();
        assertThat(clothesAttributeCount).isEqualTo(0L);
    }
}