package com.ootd.fitme.domain.attribute.service;

import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;
import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.exception.AttributeException;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("AttributeDefService 통합 테스트 (DB 연동)")
class AttributeDefServiceTest {


    @Autowired
    private AttributeDefServiceImpl service;

    @Autowired
    private AttributeRepository repository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("createClothesAttributeDef() 메서드는")
    class Describe_create {
        @Test
        @DisplayName("[성공] 실제 DB에 속성과 옵션을 순서대로 저장한다.")
        void it_saves_to_db() {
            // given
            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("사이즈", List.of("S", "M", "L"));

            // when
            ClothesAttributeDefDto result = service.createClothesAttributeDef(request);

            flushAndClear();

            // then
            Attribute savedAttribute = repository.findById(result.id()).orElseThrow();
            assertThat(savedAttribute.getName()).isEqualTo("사이즈");

            // List의 사이즈와 내부 요소들의 특정 필드(type, displayOrder)를 한 번에 우아하게 검증합니다.
            assertThat(savedAttribute.getSelectableValues())
                    .hasSize(3)
                    .extracting("type", "displayOrder")
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple("S", 0),
                            org.assertj.core.groups.Tuple.tuple("M", 1),
                            org.assertj.core.groups.Tuple.tuple("L", 2)
                    );
        }

        @Test
        @DisplayName("[실패] 이미 DB에 존재하는 이름으로 생성 시 예외를 던진다.")
        void it_throws_if_duplicated_name() {
            // given
            repository.save(Attribute.create("핏"));
            flushAndClear();

            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("핏", List.of("오버핏"));

            // when & then
            assertThatThrownBy(() -> service.createClothesAttributeDef(request))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NAME_DUPLICATED.getMessage());
        }
    }

    @Nested
    @DisplayName("updateClothesAttributeDef() 메서드는")
    class Describe_update {
        private UUID attributeId;
        private UUID originalRedValueId;

        @BeforeEach
        void setUp() {
            Attribute attribute = Attribute.create("색상");
            attribute.addValues(List.of("빨강", "파랑"));
            Attribute savedAttribute = repository.save(attribute);

            attributeId = savedAttribute.getId();
            originalRedValueId = savedAttribute.getSelectableValues().get(0).getId();

            flushAndClear();
        }

        @Test
        @DisplayName("[성공] 실제 DB의 속성 이름을 바꾸고, 옵션은 ID를 유지한 채 병합(Merge) 업데이트한다.")
        void it_updates_db_records_with_merge() {
            // given
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest(
                    "수정된색상",
                    List.of("검정", "빨강")
            );

            // when
            service.updateClothesAttributeDef(attributeId, request);
            flushAndClear();

            // then
            Attribute updatedAttribute = repository.findById(attributeId).orElseThrow();
            assertThat(updatedAttribute.getName()).isEqualTo("수정된색상");
            assertThat(updatedAttribute.getSelectableValues()).hasSize(2);

            SelectableValue blackValue = updatedAttribute.getSelectableValues().get(0);
            assertThat(blackValue.getType()).isEqualTo("검정");
            assertThat(blackValue.getDisplayOrder()).isEqualTo(0);

            SelectableValue redValue = updatedAttribute.getSelectableValues().get(1);
            assertThat(redValue.getType()).isEqualTo("빨강");
            assertThat(redValue.getDisplayOrder()).isEqualTo(1);
            assertThat(redValue.getId()).isEqualTo(originalRedValueId);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 속성 ID로 수정을 요청하면 예외를 던진다.")
        void it_throws_if_attribute_not_found() {
            // given
            UUID invalidId = UUID.randomUUID();
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest(
                    "아무이름", List.of("옵션1")
            );

            // when & then
            assertThatThrownBy(() -> service.updateClothesAttributeDef(invalidId, request))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("[실패] 이미 존재하는 다른 속성의 이름으로 수정을 시도하면 예외를 던진다(이름 중복).")
        void it_throws_if_name_is_duplicated() {
            // given
            Attribute anotherAttribute = Attribute.create("이미있는이름");
            repository.save(anotherAttribute);
            flushAndClear();

            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest(
                    "이미있는이름", List.of("검정", "흰색")
            );

            // when & then
            assertThatThrownBy(() -> service.updateClothesAttributeDef(attributeId, request))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NAME_DUPLICATED.getMessage());
        }

        @Test
        @DisplayName("[성공] 속성 업데이트로 특정 옵션이 삭제되면, 해당 옵션을 사용 중인 옷-속성-옵션 연결 데이터도 연쇄 삭제된다.")
        void it_cascades_delete_to_clothes_attribute_selectable_values_when_option_is_removed() {
            // given
            User user = User.create("abc@abc.com", "8878@asd");
            em.persist(user);

            Clothes clothes = Clothes.create("빨간 티셔츠", ClothesType.TOP, user);
            em.persist(clothes);

            Attribute savedAttribute = repository.findById(attributeId).orElseThrow();
            SelectableValue redOption = savedAttribute.getSelectableValues().stream()
                    .filter(v -> v.getType().equals("빨강"))
                    .findFirst().orElseThrow();
            UUID redOptionId = redOption.getId();

            ClothesAttribute clothesAttribute = ClothesAttribute.create(clothes, savedAttribute);
            clothesAttribute.assignOption(redOption);
            em.persist(clothesAttribute);

            flushAndClear();

            // when
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest(
                    "색상", List.of("파랑", "노랑")
            );
            service.updateClothesAttributeDef(attributeId, request);
            flushAndClear();

            // then
            Long redOptionCount = em.createQuery(
                            "SELECT COUNT(s) FROM SelectableValue s WHERE s.id = :id", Long.class)
                    .setParameter("id", redOptionId)
                    .getSingleResult();
            assertThat(redOptionCount).isEqualTo(0L);

            Long clothesAttrOptionCount = em.createQuery(
                            "SELECT COUNT(casv) FROM ClothesAttributeSelectableValue casv WHERE casv.selectableValue.id = :valId", Long.class)
                    .setParameter("valId", redOptionId)
                    .getSingleResult();
            assertThat(clothesAttrOptionCount).isEqualTo(0L);

            Long clothesCount = em.createQuery(
                            "SELECT COUNT(c) FROM Clothes c WHERE c.id = :clothesId", Long.class)
                    .setParameter("clothesId", clothes.getId())
                    .getSingleResult();
            assertThat(clothesCount).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("deleteClothesAttributeDef() 메서드는")
    class Describe_delete {
        @Test
        @DisplayName("[성공] 부모를 삭제하면 자식(옵션)까지 실제 DB에서 연쇄 삭제(Cascade) 된다.")
        void it_deletes_from_db() {
            // given
            Attribute attribute = Attribute.create("재질");
            attribute.addValues(List.of("면", "폴리"));
            UUID id = repository.save(attribute).getId();
            flushAndClear();

            // when
            service.deleteClothesAttributeDef(id);
            flushAndClear();

            // then
            assertThat(repository.findById(id)).isEmpty();
            Long optionCount = em.createQuery("SELECT COUNT(s) FROM SelectableValue s WHERE s.attribute.id = :id", Long.class)
                    .setParameter("id", id)
                    .getSingleResult();
            assertThat(optionCount).isEqualTo(0L);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 속성 ID로 삭제를 요청하면 예외를 던진다.")
        void it_throws_if_attribute_not_found() {
            // given
            UUID invalidId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> service.deleteClothesAttributeDef(invalidId))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("[성공] 속성을 삭제하면, 해당 속성을 사용 중인 옷의 연결 데이터(ClothesAttribute 등)도 완벽하게 연쇄 삭제된다.")
        void it_cascades_delete_to_clothes_attributes() {
            // given
            Attribute attribute = Attribute.create("재질");
            attribute.addValues(List.of("면", "가죽"));
            repository.save(attribute);
            UUID attributeId = attribute.getId();
            SelectableValue option = attribute.getSelectableValues().get(0);

            User user = User.create("abc@abc.com", "8878@asd");
            em.persist(user);

            Clothes clothes = Clothes.create("검정 티셔츠", ClothesType.TOP, user);
            em.persist(clothes);

            ClothesAttribute clothesAttribute = ClothesAttribute.create(clothes, attribute);
            clothesAttribute.assignOption(option);
            em.persist(clothesAttribute);

            flushAndClear();

            // when
            service.deleteClothesAttributeDef(attributeId);
            flushAndClear();

            // then
            assertThat(repository.findById(attributeId)).isEmpty();

            Long clothesAttrCount = em.createQuery(
                            "SELECT COUNT(ca) FROM ClothesAttribute ca WHERE ca.attribute.id = :id", Long.class)
                    .setParameter("id", attributeId)
                    .getSingleResult();
            assertThat(clothesAttrCount).isEqualTo(0L);

            // 3. 옷-속성-옵션
            Long clothesAttrOptionCount = em.createQuery(
                            "SELECT COUNT(casv) FROM ClothesAttributeSelectableValue casv WHERE casv.selectableValue.id = :valId", Long.class)
                    .setParameter("valId", option.getId())
                    .getSingleResult();
            assertThat(clothesAttrOptionCount).isEqualTo(0L);

            Long clothesCount = em.createQuery(
                            "SELECT COUNT(c) FROM Clothes c WHERE c.id = :clothesId", Long.class)
                    .setParameter("clothesId", clothes.getId())
                    .getSingleResult();
            assertThat(clothesCount).isEqualTo(1L);
        }


    }

    @Nested
    @DisplayName("getClothesAttributeDefs() 메서드는")
    class Describe_getClothesAttributeDefs {
        @BeforeEach
        void setUp() throws InterruptedException{
            Attribute attr1 = Attribute.create("아우터핏");
            attr1.addValues(List.of("오버핏"));
            repository.save(attr1);
            Thread.sleep(10); // 생성 시간에 미세한 차이를 두기 위해 대기

            Attribute attr2 = Attribute.create("상의사이즈");
            attr2.addValues(List.of("95", "100"));
            repository.save(attr2);
            Thread.sleep(10);

            Attribute attr3 = Attribute.create("하의사이즈");
            attr3.addValues(List.of("28", "30"));
            repository.save(attr3);

            flushAndClear();
        }

        @Test
        @DisplayName("[성공] QueryDSL을 통해 검색어(keyword)가 포함된 속성만 가져온다.")
        void it_fetches_with_keyword() {
            // when
            List<ClothesAttributeDefDto> result = service.getClothesAttributeDefs("createdAt", "DESC", "사이즈");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("name").containsExactlyInAnyOrder("상의사이즈", "하의사이즈");

            // fetchJoin
            assertThat(result.stream().filter(r -> r.name().equals("상의사이즈")).findFirst().get().selectableValues())
                    .containsExactly("95", "100");
        }

        @Test
        @DisplayName("[성공] 정렬 조건(createdAt, DESC)에 맞춰 가장 최근에 생성된 데이터를 먼저 반환한다.")
        void it_fetches_with_created_at_sorting() {
            // when
            List<ClothesAttributeDefDto> result = service.getClothesAttributeDefs("createdAt", "DESC", null);

            // then
            assertThat(result).hasSize(3);
            assertThat(result)
                    .extracting("name")
                    .containsExactly("하의사이즈", "상의사이즈", "아우터핏");
        }
    }



    // 영속성 컨텍스트 초기화 유틸 메서드
    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}