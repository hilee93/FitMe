package com.ootd.fitme.domain.clothes.service;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.dto.ClothesAttributeDto;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.clothes.enums.SortDirection;
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
import org.mockito.ArgumentCaptor;
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
@DisplayName("ClothesService 단위 테스트")
class ClothesServiceUnitTest {

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
        @DisplayName("성공: 본인의 옷을 유효한 요청으로 수정하면 성공한다. (XSS 방어 적용)")
        void updateClothes_Success() {
            // given
            UUID clothesId = UUID.randomUUID();
            UUID loginUserId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID();

            ClothesUpdateRequest request = new ClothesUpdateRequest("<script>alert('해킹')</script> 청 자켓", ClothesType.OUTER, List.of(new ClothesAttributeDto(attributeId, "DENIM")));

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(loginUserId);
            Clothes existingClothes = Clothes.createWithImage("기존 자켓", ClothesType.OUTER, mockUser, null);

            given(clothesRepository.findByIdWithDetails(clothesId)).willReturn(Optional.of(existingClothes));

            Attribute mockAttribute = mock(Attribute.class);
            given(mockAttribute.getId()).willReturn(attributeId);
            given(mockAttribute.getName()).willReturn("소재");

            SelectableValue mockSelectableValue = mock(SelectableValue.class);
            given(mockSelectableValue.getAttribute()).willReturn(mockAttribute);
            given(mockSelectableValue.getType()).willReturn("DENIM");

            given(attributeRepository.findAllById(anyList())).willReturn(List.of(mockAttribute));
            given(selectableValueRepository.findAllByAttributeIdIn(anyList())).willReturn(List.of(mockSelectableValue));

            // when
            ClothesDto result = clothesService.updateClothes(clothesId, loginUserId, request, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("&lt;script&gt;alert(&#39;해킹&#39;)&lt;/script&gt; 청 자켓");
        }

        @Test
        @DisplayName("실패: 본인이 아닌 타인이 수정을 시도하면 AUTH_FORBIDDEN 예외가 발생한다.")
        void updateClothes_Fail_Forbidden() {
            // given
            UUID clothesId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID maliciousUserId = UUID.randomUUID();

            User mockOwner = mock(User.class);
            given(mockOwner.getId()).willReturn(ownerId);
            Clothes existingClothes = Clothes.createWithImage("기존 자켓", ClothesType.OUTER, mockOwner, null);

            given(clothesRepository.findByIdWithDetails(clothesId)).willReturn(Optional.of(existingClothes));
            ClothesUpdateRequest request = new ClothesUpdateRequest("청 자켓", ClothesType.OUTER, List.of());

            // when & then: 권한 없음 예외 발생 확인
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.updateClothes(clothesId, maliciousUserId, request, null)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("옷 삭제 (Delete) 로직 테스트")
    class DeleteClothesTest {

        @Test
        @DisplayName("성공: 본인의 옷 ID로 삭제 요청 시 벌크 삭제 쿼리가 수행된다.")
        void deleteClothes_Success() {
            // given
            UUID clothesId = UUID.randomUUID();
            UUID loginUserId = UUID.randomUUID();

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(loginUserId);
            Clothes mockClothes = Clothes.createWithImage("자켓", ClothesType.OUTER, mockUser, null);

            given(clothesRepository.findById(clothesId)).willReturn(Optional.of(mockClothes));

            // when
            clothesService.deleteClothes(clothesId, loginUserId);

            // then
            then(clothesRepository).should(times(1)).deleteByIdInBulk(clothesId);
        }

        @Test
        @DisplayName("실패: 본인이 아닌 타인이 삭제를 시도하면 AUTH_FORBIDDEN 예외가 발생한다.")
        void deleteClothes_Fail_Forbidden() {
            // given
            UUID clothesId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            UUID maliciousUserId = UUID.randomUUID();

            User mockOwner = mock(User.class);
            given(mockOwner.getId()).willReturn(ownerId);
            Clothes mockClothes = Clothes.createWithImage("자켓", ClothesType.OUTER, mockOwner, null);

            given(clothesRepository.findById(clothesId)).willReturn(Optional.of(mockClothes));

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.deleteClothes(clothesId, maliciousUserId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN);
            then(clothesRepository).should(never()).deleteByIdInBulk(any());
        }
    }
    @Nested
    @DisplayName("옷 목록 조회 (Read) 로직 및 파라미터 정합성 테스트")
    class GetClothesListTest {

        @Test
        @DisplayName("성공: 올바른 파라미터로 요청하면 Repository에 조회 작업을 위임하고 결과를 반환한다.")
        void getClothesList_Success() {
            // given
            UUID loginUserId = UUID.randomUUID();
            ClothesDtoCursorRequest request = new ClothesDtoCursorRequest(
                    null, null, 20, null, loginUserId.toString(), SortBy.createdAt, SortDirection.DESCENDING
            );

            ClothesDtoCursorResponse mockResponse = new ClothesDtoCursorResponse(
                    List.of(), null, null, false, 0L, SortBy.createdAt, SortDirection.DESCENDING
            );

            // 서비스 계층 내에서 secureRequest 객체가 '새로 생성(new)'되어 넘어오기 때문에, any()를 사용합니다.
            given(clothesRepository.findClothesByCursor(any(ClothesDtoCursorRequest.class))).willReturn(mockResponse);

            // when
            ClothesDtoCursorResponse result = clothesService.getClothesList(request, loginUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.totalCount()).isEqualTo(0L);
            then(clothesRepository).should(times(1)).findClothesByCursor(any(ClothesDtoCursorRequest.class));
        }

        // 🌟 엣지 케이스 1: 가장 중요한 보안 검증 로직
        @Test
        @DisplayName("엣지 케이스 (보안): 클라이언트가 조작된 타인의 ownerId를 보내도, 서비스에서 로그인 유저의 ID로 덮어써서 Repository에 넘긴다.")
        void getClothesList_Overwrite_MaliciousOwnerId() {
            // given
            UUID loginUserId = UUID.randomUUID();
            UUID maliciousTargetId = UUID.randomUUID();

            ClothesDtoCursorRequest hackedRequest = new ClothesDtoCursorRequest(
                    null, null, 20, null, maliciousTargetId.toString(), SortBy.createdAt, SortDirection.DESCENDING
            );

            ClothesDtoCursorResponse mockResponse = new ClothesDtoCursorResponse(
                    List.of(), null, null, false, 0L, SortBy.createdAt, SortDirection.DESCENDING
            );
            given(clothesRepository.findClothesByCursor(any(ClothesDtoCursorRequest.class))).willReturn(mockResponse);

            // when
            clothesService.getClothesList(hackedRequest, loginUserId);

            // then: ArgumentCaptor를 사용해 Repository로 최종 전달된 객체를 낚아챕니다.
            ArgumentCaptor<ClothesDtoCursorRequest> requestCaptor = ArgumentCaptor.forClass(ClothesDtoCursorRequest.class);
            then(clothesRepository).should(times(1)).findClothesByCursor(requestCaptor.capture());

            ClothesDtoCursorRequest securedRequest = requestCaptor.getValue();

            assertThat(securedRequest.ownerId()).isEqualTo(loginUserId.toString());
            assertThat(securedRequest.ownerId()).isNotEqualTo(maliciousTargetId.toString());
        }

        // 🌟 엣지 케이스 2: 내부 시스템 무결성 검증
        @Test
        @DisplayName("실패: 내부 에러 등으로 loginUserId 자체가 누락된 채 호출되면 INVALID_REQUEST 예외를 발생시킨다.")
        void getClothesList_Fail_LoginUserIdMissing() {
            // given
            ClothesDtoCursorRequest request = new ClothesDtoCursorRequest(
                    null, null, 20, null, UUID.randomUUID().toString(), SortBy.createdAt, SortDirection.DESCENDING
            );

            // when & then: loginUserId 자리에 null이 들어왔을 때의 방어
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.getClothesList(request, null)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
            then(clothesRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패: 커서(cursor)만 있고 idAfter가 없는 불완전한 파라미터 요청은 INVALID_INPUT_VALUE 예외를 발생시킨다.")
        void getClothesList_Fail_CursorWithoutIdAfter() {
            // given
            UUID loginUserId = UUID.randomUUID();
            ClothesDtoCursorRequest request = new ClothesDtoCursorRequest(
                    "2026-04-02T10:00:00Z|가_셔츠", null, 20, null, loginUserId.toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.getClothesList(request, loginUserId)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
            then(clothesRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("실패: idAfter만 있고 커서(cursor)가 없는 불완전한 파라미터 요청은 INVALID_INPUT_VALUE 예외를 발생시킨다.")
        void getClothesList_Fail_IdAfterWithoutCursor() {
            // given
            UUID loginUserId = UUID.randomUUID();
            ClothesDtoCursorRequest request = new ClothesDtoCursorRequest(
                    "", UUID.randomUUID().toString(), 20, null, loginUserId.toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.getClothesList(request, loginUserId)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
            then(clothesRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("비즈니스 데이터 무결성 엣지 케이스 테스트")
    class DataIntegrityEdgeCaseTest {

        @Test
        @DisplayName("실패: 옷을 생성할 때 존재하지 않는 속성(Attribute) ID를 보내면 예외가 발생한다.")
        void createClothes_Fail_AttributeNotFound() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID invalidAttributeId = UUID.randomUUID(); // DB에 없는 속성
            ClothesAttributeDto attrDto = new ClothesAttributeDto(invalidAttributeId, "COTTON");
            ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "흰 셔츠", ClothesType.TOP, List.of(attrDto));

            User mockUser = mock(User.class);
            given(userRepository.findById(ownerId)).willReturn(Optional.of(mockUser));

            // 레포지토리에서 조회 시 텅 빈 리스트 반환 (속성을 못 찾음)
            given(attributeRepository.findAllById(anyList())).willReturn(List.of());

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.createClothes(request, null)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ATTRIBUTE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 옷을 생성할 때 속성은 존재하지만 선택한 옵션(SelectableValue)이 잘못되면 예외가 발생한다.")
        void createClothes_Fail_OptionNotFound() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID validAttributeId = UUID.randomUUID();
            ClothesAttributeDto attrDto = new ClothesAttributeDto(validAttributeId, "WRONG_OPTION"); // 존재하지 않는 옵션 텍스트
            ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "흰 셔츠", ClothesType.TOP, List.of(attrDto));

            User mockUser = mock(User.class);
            given(userRepository.findById(ownerId)).willReturn(Optional.of(mockUser));

            Attribute mockAttribute = mock(Attribute.class);
            given(mockAttribute.getId()).willReturn(validAttributeId);
            given(attributeRepository.findAllById(anyList())).willReturn(List.of(mockAttribute));

            // 옵션(SelectableValue) 조회 시 빈 리스트 반환
            given(selectableValueRepository.findAllByAttributeIdIn(anyList())).willReturn(List.of());

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.createClothes(request, null)
            );

            // OPTION_NOT_FOUND 에러코드 발생 확인
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.OPTION_NOT_FOUND);
        }
    }
}