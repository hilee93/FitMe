package com.ootd.fitme.domain.clothes.service;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.dto.ClothesAttributeDto;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.exception.ClothesException;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.domain.selectablevalue.repository.SelectableValueRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClothesService CUD 단위 테스트")
class ClothesServiceUnitCUDTest {

    @InjectMocks
    private ClothesServiceImpl clothesService;

    @Mock
    private ClothesRepository clothesRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AttributeRepository attributeRepository;
    @Mock
    private SelectableValueRepository selectableValueRepository;

    @Nested
    @DisplayName("옷 등록 (Create) 로직 테스트")
    class CreateClothesTest {

        @Test
        @DisplayName("성공: 유효한 요청이 들어오면 옷을 성공적으로 등록하고 DTO를 반환한다.")
        void createClothes_Success() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID();
            ClothesAttributeDto attrDto = new ClothesAttributeDto(attributeId, "COTTON");
            ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "흰 셔츠", ClothesType.TOP, List.of(attrDto));

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(ownerId);
            given(userRepository.findById(ownerId)).willReturn(Optional.of(mockUser));

            // Attribute와 SelectableValue 모의 세팅
            Attribute mockAttribute = mock(Attribute.class);
            given(mockAttribute.getId()).willReturn(attributeId);
            given(mockAttribute.getName()).willReturn("소재");

            SelectableValue mockSelectableValue = mock(SelectableValue.class);
            given(mockSelectableValue.getAttribute()).willReturn(mockAttribute);
            given(mockSelectableValue.getType()).willReturn("COTTON");

            given(attributeRepository.findAllById(anyList())).willReturn(List.of(mockAttribute));
            given(selectableValueRepository.findAllByAttributeIdIn(anyList())).willReturn(List.of(mockSelectableValue));

            Clothes mockSavedClothes = Clothes.createWithImage(request.name(), request.type(), mockUser, null);
            given(clothesRepository.save(any(Clothes.class))).willReturn(mockSavedClothes);

            // when
            ClothesDto result = clothesService.createClothes(request, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("흰 셔츠");
            then(clothesRepository).should(times(1)).save(any(Clothes.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자의 ID로 요청하면 예외가 발생한다.")
        void createClothes_Fail_UserNotFound() {
            // given
            UUID invalidOwnerId = UUID.randomUUID();
            ClothesCreateRequest request = new ClothesCreateRequest(invalidOwnerId, "흰 셔츠", ClothesType.TOP, List.of());

            given(userRepository.findById(invalidOwnerId)).willReturn(Optional.empty());

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.createClothes(request, null)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CLOTHES_OWNER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("옷 수정 (Update) 로직 테스트")
    class UpdateClothesTest {

        @Test
        @DisplayName("성공: 유효한 요청으로 옷 정보를 수정하면 성공한다.")
        void updateClothes_Success() {
            // given
            UUID clothesId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID();
            ClothesAttributeDto attrDto = new ClothesAttributeDto(attributeId, "DENIM");
            ClothesUpdateRequest request = new ClothesUpdateRequest("청 자켓", ClothesType.OUTER, List.of(attrDto));

            User mockUser = mock(User.class);
            Clothes existingClothes = Clothes.createWithImage("기존 자켓", ClothesType.OUTER, mockUser, null);
            given(clothesRepository.findById(clothesId)).willReturn(Optional.of(existingClothes));

            // Attribute와 SelectableValue 모의 세팅 (buildClothesAttributes를 무사히 통과하기 위함)
            Attribute mockAttribute = mock(Attribute.class);
            given(mockAttribute.getId()).willReturn(attributeId);
            given(mockAttribute.getName()).willReturn("소재");

            SelectableValue mockSelectableValue = mock(SelectableValue.class);
            given(mockSelectableValue.getAttribute()).willReturn(mockAttribute);
            given(mockSelectableValue.getType()).willReturn("DENIM");

            given(attributeRepository.findAllById(anyList())).willReturn(List.of(mockAttribute));
            given(selectableValueRepository.findAllByAttributeIdIn(anyList())).willReturn(List.of(mockSelectableValue));

            // 응답 DTO 생성을 위한 세팅
            given(attributeRepository.getReferenceById(attributeId)).willReturn(mockAttribute);

            // when
            ClothesDto result = clothesService.updateClothes(clothesId, request, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("청 자켓");
        }

        @Test
        @DisplayName("실패: 수정하려는 옷이 DB에 없으면 예외가 발생한다.")
        void updateClothes_Fail_ClothesNotFound() {
            // given
            UUID invalidClothesId = UUID.randomUUID();
            ClothesUpdateRequest request = new ClothesUpdateRequest("청 자켓", ClothesType.OUTER, List.of());

            given(clothesRepository.findById(invalidClothesId)).willReturn(Optional.empty());

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.updateClothes(invalidClothesId, request, null)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CLOTHES_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("옷 삭제 (Delete) 로직 테스트")
    class DeleteClothesTest {

        @Test
        @DisplayName("성공: 존재하는 옷 ID로 삭제 요청 시 벌크 삭제 쿼리가 수행된다.")
        void deleteClothes_Success() {
            // given (준비)
            UUID clothesId = UUID.randomUUID();

            given(clothesRepository.existsById(clothesId)).willReturn(true);

            // when (실행)
            clothesService.deleteClothes(clothesId);

            // then (검증)
            then(clothesRepository).should(times(1)).deleteByIdInBulk(clothesId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 옷 ID로 삭제 요청 시 예외가 발생한다.")
        void deleteClothes_Fail_ClothesNotFound() {
            // given (준비)
            UUID invalidClothesId = UUID.randomUUID();

            given(clothesRepository.existsById(invalidClothesId)).willReturn(false);

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.deleteClothes(invalidClothesId)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CLOTHES_NOT_FOUND);

            then(clothesRepository).should(never()).deleteByIdInBulk(any());
        }
    }
}