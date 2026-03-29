package com.ootd.fitme.domain.attribute.service;

import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;
import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.exception.AttributeException;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
            assertThat(savedAttribute.getSelectableValues()).hasSize(3);
            assertThat(savedAttribute.getSelectableValues().get(0).getType()).isEqualTo("S");
            assertThat(savedAttribute.getSelectableValues().get(2).getType()).isEqualTo("L");
            assertThat(savedAttribute.getSelectableValues().get(2).getDisplayOrder()).isEqualTo(2);
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

        @BeforeEach
        void setUp() {
            Attribute attribute = Attribute.create("색상");
            attribute.addValues(List.of("빨강", "파랑"));
            attributeId = repository.save(attribute).getId();
            flushAndClear();
        }

        @Test
        @DisplayName("[성공] 실제 DB의 속성 이름과 옵션(Clear & Insert)을 업데이트한다.")
        void it_updates_db_records() {
            // given
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("수정된색상", List.of("검정", "흰색", "회색"));

            // when
            service.updateClothesAttributeDef(attributeId, request);
            flushAndClear(); // 쿼리 전송

            // then
            Attribute updatedAttribute = repository.findById(attributeId).orElseThrow();
            assertThat(updatedAttribute.getName()).isEqualTo("수정된색상");
            assertThat(updatedAttribute.getSelectableValues()).hasSize(3);
            assertThat(updatedAttribute.getSelectableValues().get(0).getType()).isEqualTo("검정");
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
    }

    @Nested
    @DisplayName("getClothesAttributeDefs() 메서드는")
    class Describe_getClothesAttributeDefs {
        @BeforeEach
        void setUp() {
            Attribute attr1 = Attribute.create("아우터핏");
            attr1.addValues(List.of("오버핏"));
            Attribute attr2 = Attribute.create("상의사이즈");
            attr2.addValues(List.of("95", "100"));
            Attribute attr3 = Attribute.create("하의사이즈");
            attr3.addValues(List.of("28", "30"));

            repository.saveAll(List.of(attr1, attr2, attr3));
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
        @DisplayName("[성공] 정렬 조건(name, ASC)에 맞춰 데이터를 반환한다.")
        void it_fetches_with_sorting() {
            // when
            List<ClothesAttributeDefDto> result = service.getClothesAttributeDefs("name", "ASC", null);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).name()).isEqualTo("상의사이즈"); // ㅅ
            assertThat(result.get(1).name()).isEqualTo("아우터핏");   // ㅇ
            assertThat(result.get(2).name()).isEqualTo("하의사이즈"); // ㅎ
        }
    }

    // 영속성 컨텍스트 초기화 유틸 메서드
    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}