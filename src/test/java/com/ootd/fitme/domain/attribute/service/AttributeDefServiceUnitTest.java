package com.ootd.fitme.domain.attribute.service;

import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;
import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.exception.AttributeException;
import com.ootd.fitme.domain.attribute.mapper.AttributeMapper;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttributeDefServiceImpl 단위 테스트")
class AttributeDefServiceUnitTest {

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private AttributeMapper attributeMapper;

    @InjectMocks
    private AttributeDefServiceImpl service;

    private UUID attributeId;
    private Attribute testAttribute;
    private ClothesAttributeDefDto mockDto;

    @BeforeEach
    void setUp() {
        attributeId = UUID.randomUUID();
        testAttribute = Attribute.create("사이즈");
        ReflectionTestUtils.setField(testAttribute, "id", attributeId);

        mockDto = new ClothesAttributeDefDto(attributeId, "사이즈", List.of("S", "M"), Instant.now());
    }

    @Nested
    @DisplayName("getClothesAttributeDefs() 메서드는")
    class Describe_getClothesAttributeDefs {
        @Test
        @DisplayName("[성공] 정렬 및 검색 조건에 맞는 속성 목록을 반환한다.")
        void it_returns_attribute_list() {
            // given
            given(attributeRepository.findAttributesWithCondition("createdAt", "DESC", "사이즈"))
                    .willReturn(List.of(testAttribute));
            given(attributeMapper.toDto(testAttribute)).willReturn(mockDto);

            // when
            List<ClothesAttributeDefDto> result = service.getClothesAttributeDefs("createdAt", "DESC", "사이즈");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("사이즈");
            verify(attributeRepository).findAttributesWithCondition("createdAt", "DESC", "사이즈");
        }
    }

    @Nested
    @DisplayName("createClothesAttributeDef() 메서드는")
    class Describe_create {
        @Test
        @DisplayName("[성공] 중복되지 않은 이름이면 속성을 생성하고 반환한다.")
        void it_creates_attribute() {
            // given
            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("핏", List.of("오버핏"));
            given(attributeRepository.existsByName("핏")).willReturn(false);
            given(attributeRepository.save(any(Attribute.class))).willReturn(testAttribute);
            given(attributeMapper.toDto(any(Attribute.class))).willReturn(mockDto);

            // when
            ClothesAttributeDefDto result = service.createClothesAttributeDef(request);

            // then
            assertThat(result).isNotNull();
            verify(attributeRepository).save(any(Attribute.class));
        }

        @Test
        @DisplayName("[실패] 이미 존재하는 이름이면 ATTRIBUTE_NAME_DUPLICATED 예외를 던진다.")
        void it_throws_exception_if_duplicated() {
            // given
            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("중복된이름", List.of());
            given(attributeRepository.existsByName("중복된이름")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> service.createClothesAttributeDef(request))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NAME_DUPLICATED.getMessage());

            verify(attributeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateClothesAttributeDef() 메서드는")
    class Describe_update {
        @Test
        @DisplayName("[성공] 존재하는 ID이고 중복되지 않은 이름이면 정보를 수정(Merge)한다.")
        void it_updates_attribute() {
            // given
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("새로운사이즈", List.of("L"));
            given(attributeRepository.findById(attributeId)).willReturn(Optional.of(testAttribute));
            given(attributeRepository.existsByName("새로운사이즈")).willReturn(false); // 이름이 달라서 중복 검사 탐
            given(attributeMapper.toDto(testAttribute)).willReturn(mockDto);

            // when
            service.updateClothesAttributeDef(attributeId, request);

            // then
            assertThat(testAttribute.getName()).isEqualTo("새로운사이즈");
            verify(attributeMapper).toDto(testAttribute);
            // 참고: 내부 옵션(SelectableValue)이 Merge되는 로직은 엔티티 단위 테스트에서 이미 검증 완료됨
        }

        @Test
        @DisplayName("[성공] 이름이 기존과 동일하면 중복 검사를 패스하고 옵션만 수정한다.")
        void it_updates_options_only_if_name_is_same() {
            // given
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("사이즈", List.of("XL")); // 기존 이름과 동일
            given(attributeRepository.findById(attributeId)).willReturn(Optional.of(testAttribute));
            given(attributeMapper.toDto(testAttribute)).willReturn(mockDto);

            // when
            service.updateClothesAttributeDef(attributeId, request);

            // then
            verify(attributeRepository, never()).existsByName("사이즈"); // 중복 검사 호출 안 됨!
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 ID면 ATTRIBUTE_NOT_FOUND 예외를 던진다.")
        void it_throws_not_found() {
            // given
            given(attributeRepository.findById(attributeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.updateClothesAttributeDef(attributeId, new ClothesAttributeDefUpdateRequest("이름", List.of())))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("[실패] 변경하려는 이름이 이미 다른 곳에서 사용 중이면 ATTRIBUTE_NAME_DUPLICATED 예외를 던진다.")
        void it_throws_duplicate_name() {
            // given
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("중복사이즈", List.of());
            given(attributeRepository.findById(attributeId)).willReturn(Optional.of(testAttribute));
            given(attributeRepository.existsByName("중복사이즈")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> service.updateClothesAttributeDef(attributeId, request))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NAME_DUPLICATED.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteClothesAttributeDef() 메서드는")
    class Describe_delete {

        @Test
        @DisplayName("[성공] 존재하는 ID가 주어지면 N+1 방지를 위해 벌크 삭제(deleteByIdInBulk)를 수행한다.")
        void it_deletes_attribute() {
            // given
            given(attributeRepository.findById(attributeId)).willReturn(Optional.of(testAttribute));

            // when
            service.deleteClothesAttributeDef(attributeId);

            // then
            verify(attributeRepository).deleteByIdInBulk(attributeId);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 ID면 삭제하지 않고 예외를 던진다.")
        void it_throws_not_found() {
            // given
            given(attributeRepository.findById(attributeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.deleteClothesAttributeDef(attributeId))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NOT_FOUND.getMessage());

            verify(attributeRepository, never()).deleteByIdInBulk(any());
        }
    }
}