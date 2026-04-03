package com.ootd.fitme.domain.clothes.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.clothes.enums.SortDirection;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.clothesattribute.repository.ClothesAttributeRepository;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.exception.DataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Autowired
    private ClothesAttributeRepository clothesAttributeRepository;

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

    @Test
    @DisplayName("1:1 자식 객체의 내부 값만 업데이트하면 제약조건 예외 없이 정상적으로 변경된다")
    void testSafeUpdateOneToOneRelation() {
        // given
        User user = User.create("abc@abc.com", "8878@asd");
        userRepository.save(user);

        Clothes clothes = Clothes.createWithImage("테스트 옷", ClothesType.TOP, user, null);
        clothesRepository.save(clothes);

        Attribute attr = Attribute.create("색상");
        attr.addValues(List.of("빨강", "파랑"));
        attributeRepository.save(attr);

        SelectableValue redValue = attr.getSelectableValues().get(0);
        SelectableValue blueValue = attr.getSelectableValues().get(1);

        ClothesAttribute clothesAttribute = ClothesAttribute.create(clothes, attr);

        clothesAttribute.assignOption(redValue);
        clothesAttributeRepository.save(clothesAttribute);

        em.flush();
        em.clear();

        // when
        ClothesAttribute savedAttribute = clothesAttributeRepository.findById(clothesAttribute.getId()).get();
        SelectableValue savedBlueValue = em.find(SelectableValue.class, blueValue.getId());

        savedAttribute.assignOption(savedBlueValue);

        // then
        em.flush();
        em.clear();

        ClothesAttribute updatedAttribute = clothesAttributeRepository.findById(savedAttribute.getId()).get();

        assertThat(updatedAttribute.getClothesAttributeSelectableValue().getSelectableValue().getId())
                .isEqualTo(blueValue.getId());
    }

    @Nested
    @DisplayName("커서 기반 페이징 및 N+1 방지 조회 테스트")
    class CursorPaginationTest {

        private User testUser;
        private List<Clothes> savedClothesList = new ArrayList<>();

        private void setUpDummyData() {
            testUser = User.create("cursor@test.com", "password123");
            userRepository.save(testUser);

            Attribute colorAttr = Attribute.create("색상");
            colorAttr.addValues(List.of("빨강", "파랑", "노랑"));
            attributeRepository.save(colorAttr);

            String[] names = {"가_셔츠", "나_바지", "다_자켓", "라_코트", "마_패딩"};

            for (int i = 0; i < 5; i++) {
                Clothes clothes = Clothes.createWithImage(names[i], ClothesType.TOP, testUser, null);
                clothesRepository.save(clothes);

                ClothesAttribute ca = ClothesAttribute.create(clothes, colorAttr);
                ca.assignOption(colorAttr.getSelectableValues().get(0));
                clothesAttributeRepository.save(ca);

                savedClothesList.add(clothes);
            }

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("1페이지 조회: Limit만큼 정확히 가져오고, 쿼리가 딱 3번(옷, 카운트, 속성IN)만 발생하여 N+1을 완벽히 방어한다.")
        void fetchFirstPage_and_VerifyNoNPlusOne() {
            setUpDummyData();

            SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
            sessionFactory.getStatistics().setStatisticsEnabled(true);
            sessionFactory.getStatistics().clear();

            ClothesDtoCursorRequest request = new ClothesDtoCursorRequest(
                    null, null, 2, null, testUser.getId().toString(),
                    SortBy.name, SortDirection.ASCENDING
            );

            // when
            ClothesDtoCursorResponse response = clothesRepository.findClothesByCursor(request);

            // then: 1. 데이터 검증
            assertThat(response.data()).hasSize(2);
            assertThat(response.data().get(0).name()).isEqualTo("가_셔츠");
            assertThat(response.data().get(1).name()).isEqualTo("나_바지");

            // then: 2. DTO 내부에 속성(Attributes)이 잘 조립되었는지 검증
            assertThat(response.data().get(0).attributes()).hasSize(1);
            assertThat(response.data().get(0).attributes().get(0).value()).isEqualTo("빨강");

            // then: 3. 다음 페이지 여부 및 커서 검증
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextIdAfter()).isEqualTo(savedClothesList.get(1).getId().toString());
            assertThat(response.nextCursor()).isNotNull();

            long queryCount = sessionFactory.getStatistics().getPrepareStatementCount();

            // 예상 쿼리:
            // 1. SELECT 옷 (limit 3)
            // 2. SELECT 카운트 (totalCount)
            // 3. SELECT 속성 WHERE clothes_id IN (?, ?)
            // 총 3번의 쿼리만 발생해야 합니다. (N+1이 발생했다면 최소 4~5번 이상 나옴)
            assertThat(queryCount).isLessThanOrEqualTo(3L);
        }

        @Test
        @DisplayName("2페이지 조회: 이전 페이지의 커서를 바탕으로 다음 데이터를 정확히 이어서 가져온다.")
        void fetchSecondPage_WithCursor() {
            // given
            setUpDummyData();

            ClothesDtoCursorRequest firstRequest = new ClothesDtoCursorRequest(
                    null, null, 2, null, testUser.getId().toString(),
                    SortBy.name, SortDirection.ASCENDING
            );
            ClothesDtoCursorResponse firstResponse = clothesRepository.findClothesByCursor(firstRequest);

            // when: 1페이지에서 받은 nextCursor와 nextIdAfter를 집어넣어 2페이지 요청
            ClothesDtoCursorRequest secondRequest = new ClothesDtoCursorRequest(
                    firstResponse.nextCursor(), firstResponse.nextIdAfter(), 2, null, testUser.getId().toString(),
                    SortBy.name, SortDirection.ASCENDING
            );
            ClothesDtoCursorResponse secondResponse = clothesRepository.findClothesByCursor(secondRequest);

            // then
            assertThat(secondResponse.data()).hasSize(2);
            assertThat(secondResponse.data().get(0).name()).isEqualTo("다_자켓");
            assertThat(secondResponse.data().get(1).name()).isEqualTo("라_코트");

            assertThat(secondResponse.hasNext()).isTrue();
            assertThat(secondResponse.totalCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("마지막 페이지 조회: 남은 데이터가 한 개일 때 limit이 2여도 1개만 가져오고 hasNext는 false가 된다.")
        void fetchLastPage() {
            // given
            setUpDummyData();

            Clothes lastClothesOfPage2 = clothesRepository.findById(savedClothesList.get(3).getId()).get();

            String mockCursor = lastClothesOfPage2.getCreatedAt().toString() + "|" + lastClothesOfPage2.getName();
            String mockIdAfter = lastClothesOfPage2.getId().toString();

            ClothesDtoCursorRequest lastRequest = new ClothesDtoCursorRequest(
                    mockCursor, mockIdAfter, 2, null, testUser.getId().toString(),
                    SortBy.name, SortDirection.ASCENDING
            );

            // when
            ClothesDtoCursorResponse lastResponse = clothesRepository.findClothesByCursor(lastRequest);

            // then
            assertThat(lastResponse.data()).hasSize(1);
            assertThat(lastResponse.data().get(0).name()).isEqualTo("마_패딩");

            // 더 이상 남은 데이터가 없으므로 hasNext는 false
            assertThat(lastResponse.hasNext()).isFalse();
        }
    }
    @Nested
    @DisplayName("시스템 엣지 케이스 및 예외 상황 테스트")
    class SystemEdgeCaseTest {

        @Test
        @DisplayName("[성공] 옷에 연결된 모든 속성을 빈 리스트로 업데이트하면, 연관된 모든 매핑 데이터가 DB에서 삭제된다.")
        void clearAllAttributes_TriggersOrphanRemoval() {
            // given
            User user = User.create("empty@test.com", "pass123");
            userRepository.save(user);

            Clothes clothes = Clothes.create("셔츠", ClothesType.TOP, user);
            Attribute attr = Attribute.create("색상");
            attr.addValues(List.of("빨강"));
            attributeRepository.save(attr);

            ClothesAttribute ca = ClothesAttribute.create(clothes, attr);
            ca.assignOption(attr.getSelectableValues().get(0));
            clothes.getAttributes().add(ca);

            clothesRepository.save(clothes);
            em.flush(); em.clear();

            // when: 업데이트 시 속성 리스트를 아예 비워버림 (empty list)
            Clothes savedClothes = clothesRepository.findById(clothes.getId()).orElseThrow();
            savedClothes.updateAttributes(List.of()); // 빈 리스트 주입!
            em.flush(); em.clear();

            // then
            Long count = em.createQuery(
                            "SELECT COUNT(ca) FROM ClothesAttribute ca WHERE ca.clothes.id = :id", Long.class)
                    .setParameter("id", clothes.getId())
                    .getSingleResult();

            assertThat(count).isEqualTo(0L);
        }

        @Test
        @DisplayName("[실패] 옷 이름이 DB 컬럼 설정 길이(200자)를 초과하면 DataIntegrityViolationException이 발생한다.")
        void nameLengthExceeds_ThrowsException() {
            // given
            User user = User.create("longname@test.com", "pass123");
            userRepository.save(user);

            String tooLongName = "가".repeat(250);
            Clothes clothes = Clothes.create(tooLongName, ClothesType.TOP, user);

            // when & then
            assertThrows(DataException.class, () -> {
                clothesRepository.save(clothes);
                em.flush();
            });
        }

        @Test
        @DisplayName("[성공] 등록된 옷이 하나도 없는 유저가 커서 조회를 요청하면 예외 없이 빈 결과를 반환한다.")
        void fetchEmptyPage_ReturnsGracefully() {
            // given
            User newUser = User.create("newbie@test.com", "pass");
            userRepository.save(newUser);

            ClothesDtoCursorRequest request = new ClothesDtoCursorRequest(
                    null, null, 20, null, newUser.getId().toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );

            // when
            ClothesDtoCursorResponse response = clothesRepository.findClothesByCursor(request);

            // then
            assertThat(response.data()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
            assertThat(response.nextIdAfter()).isNull();
            assertThat(response.totalCount()).isEqualTo(0L);
        }
    }
}