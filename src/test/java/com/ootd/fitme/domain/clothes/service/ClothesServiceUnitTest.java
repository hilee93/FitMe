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
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import com.ootd.fitme.infrastructure.storage.image.event.FileDeleteEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

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

    @Mock private ClothesRepository clothesRepository;
    @Mock private UserRepository userRepository;
    @Mock private AttributeRepository attributeRepository;
    @Mock private SelectableValueRepository selectableValueRepository;
    @Mock private ImageStorage imageStorage;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("옷 등록 (Create) 기본 로직 테스트")
    class CreateClothesTest {

        @Test
        @DisplayName("성공: 이미지가 포함된 유효한 요청 시 스토리지를 업로드하고 옷을 등록한다.")
        void createClothes_Success() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID();
            ClothesAttributeDto attrDto = new ClothesAttributeDto(attributeId, "COTTON");
            ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "흰 셔츠", ClothesType.TOP, List.of(attrDto));

            MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "img content".getBytes());

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(ownerId);
            given(userRepository.findById(ownerId)).willReturn(Optional.of(mockUser));

            Attribute mockAttribute = mock(Attribute.class);
            given(mockAttribute.getId()).willReturn(attributeId);
            given(mockAttribute.getName()).willReturn("소재");

            SelectableValue mockSelectableValue = mock(SelectableValue.class);
            given(mockSelectableValue.getAttribute()).willReturn(mockAttribute);
            given(mockSelectableValue.getType()).willReturn("COTTON");

            given(attributeRepository.findAllById(anyList())).willReturn(List.of(mockAttribute));
            given(selectableValueRepository.findAllByAttributeIdIn(anyList())).willReturn(List.of(mockSelectableValue));

            String uploadedUrl = "https://cdn.fitme.com/img/clothes/test.jpg";
            given(imageStorage.upload(mockImage, "clothes")).willReturn(uploadedUrl);

            Clothes mockSavedClothes = Clothes.createWithImage(request.name(), request.type(), mockUser, uploadedUrl);
            given(clothesRepository.save(any(Clothes.class))).willReturn(mockSavedClothes);

            // when
            ClothesDto result = clothesService.createClothes(request, mockImage, ownerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("흰 셔츠");
            then(imageStorage).should(times(1)).upload(mockImage, "clothes");
            then(eventPublisher).should(never()).publishEvent(any());
            then(clothesRepository).should(times(1)).save(any(Clothes.class));
        }



        @Test
        @DisplayName("실패: DB에 존재하지 않는 사용자의 ID로 요청하면 예외가 발생한다.")
        void createClothes_Fail_UserNotFound() {
            // given
            UUID invalidOwnerId = UUID.randomUUID();
            ClothesCreateRequest request = new ClothesCreateRequest(invalidOwnerId, "흰 셔츠", ClothesType.TOP, List.of());

            given(userRepository.findById(invalidOwnerId)).willReturn(Optional.empty());

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.createClothes(request, null, invalidOwnerId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CLOTHES_OWNER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("옷 수정/삭제 (Update & Delete) 기본 로직 테스트")
    class UpdateAndDeleteClothesTest {

        @Test
        @DisplayName("수정 성공: 본인의 옷을 유효한 요청으로 수정하면 성공한다.")
        void updateClothes_Success() {
            // given
            UUID clothesId = UUID.randomUUID();
            UUID loginUserId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID();

            ClothesUpdateRequest request = new ClothesUpdateRequest("청 자켓", ClothesType.OUTER, List.of(new ClothesAttributeDto(attributeId, "DENIM")));

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
            assertThat(result.name()).isEqualTo("청 자켓");
        }

        @Test
        @DisplayName("수정 성공: 새 이미지가 전달되면 기존 이미지를 삭제하는 이벤트를 발행한다.")
        void updateClothes_Success_WithNewImage() {
            // given
            UUID clothesId = UUID.randomUUID();
            UUID loginUserId = UUID.randomUUID();
            String oldImageUrl = "https://cdn.fitme.com/old.jpg";

            ClothesUpdateRequest request = new ClothesUpdateRequest("청 자켓", ClothesType.OUTER, List.of());
            MockMultipartFile mockNewImage = new MockMultipartFile("image", "new.jpg", "image/jpeg", "new img".getBytes());

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(loginUserId);
            Clothes existingClothes = Clothes.createWithImage("기존 자켓", ClothesType.OUTER, mockUser, oldImageUrl);

            given(clothesRepository.findByIdWithDetails(clothesId)).willReturn(Optional.of(existingClothes));

            String newImageUrl = "https://cdn.fitme.com/new.jpg";
            given(imageStorage.upload(mockNewImage, "clothes")).willReturn(newImageUrl);

            // when
            ClothesDto result = clothesService.updateClothes(clothesId, loginUserId, request, mockNewImage);

            // then
            assertThat(result.name()).isEqualTo("청 자켓");
            then(imageStorage).should(times(1)).upload(mockNewImage, "clothes");

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            then(eventPublisher).should(times(1)).publishEvent(eventCaptor.capture());

            Object capturedEvent = eventCaptor.getValue();

            assertThat(capturedEvent).isInstanceOf(FileDeleteEvent.class);

            assertThat(((FileDeleteEvent) capturedEvent).fileUrl()).isEqualTo(oldImageUrl);
        }

        @Test
        @DisplayName("삭제 성공: 본인의 옷 ID로 삭제 요청 시 DB 데이터 삭제 및 이미지 삭제 이벤트를 발행한다.")
        void deleteClothes_Success() {
            // given
            UUID clothesId = UUID.randomUUID();
            UUID loginUserId = UUID.randomUUID();
            String imageUrlToDelete = "https://cdn.fitme.com/delete_me.jpg";

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(loginUserId);
            Clothes mockClothes = Clothes.createWithImage("자켓", ClothesType.OUTER, mockUser, imageUrlToDelete);

            given(clothesRepository.findById(clothesId)).willReturn(Optional.of(mockClothes));

            // when
            clothesService.deleteClothes(clothesId, loginUserId);

            // then
            then(clothesRepository).should(times(1)).deleteByIdInBulk(clothesId);

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            Object capturedEvent = eventCaptor.getValue();

            assertThat(capturedEvent).isInstanceOf(FileDeleteEvent.class);
            assertThat(((FileDeleteEvent) capturedEvent).fileUrl()).isEqualTo("https://cdn.fitme.com/delete_me.jpg");
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

            given(clothesRepository.findClothesByCursor(any(ClothesDtoCursorRequest.class))).willReturn(mockResponse);

            // when
            ClothesDtoCursorResponse result = clothesService.getClothesList(request, loginUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.totalCount()).isEqualTo(0L);
            then(clothesRepository).should(times(1)).findClothesByCursor(any(ClothesDtoCursorRequest.class));
        }

        @Test
        @DisplayName("실패: 내부 에러 등으로 loginUserId 자체가 누락된 채 호출되면 INVALID_REQUEST 예외를 발생시킨다.")
        void getClothesList_Fail_LoginUserIdMissing() {
            // given
            ClothesDtoCursorRequest request = new ClothesDtoCursorRequest(
                    null, null, 20, null, UUID.randomUUID().toString(), SortBy.createdAt, SortDirection.DESCENDING
            );

            // when & then
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

            given(attributeRepository.findAllById(anyList())).willReturn(List.of());

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.createClothes(request, null, ownerId) // 권한 통과를 위해 ownerId 전송
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ATTRIBUTE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 옷을 생성할 때 속성은 존재하지만 선택한 옵션(SelectableValue)이 잘못되면 예외가 발생한다.")
        void createClothes_Fail_OptionNotFound() {
            // given
            UUID ownerId = UUID.randomUUID();
            UUID validAttributeId = UUID.randomUUID();
            ClothesAttributeDto attrDto = new ClothesAttributeDto(validAttributeId, "WRONG_OPTION");
            ClothesCreateRequest request = new ClothesCreateRequest(ownerId, "흰 셔츠", ClothesType.TOP, List.of(attrDto));

            User mockUser = mock(User.class);
            given(userRepository.findById(ownerId)).willReturn(Optional.of(mockUser));

            Attribute mockAttribute = mock(Attribute.class);
            given(mockAttribute.getId()).willReturn(validAttributeId);
            given(attributeRepository.findAllById(anyList())).willReturn(List.of(mockAttribute));

            // 옵션 조회 시 빈 리스트 반환
            given(selectableValueRepository.findAllByAttributeIdIn(anyList())).willReturn(List.of());

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.createClothes(request, null, ownerId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.OPTION_NOT_FOUND);
        }
    }



    @Nested
    @DisplayName("보안 및 권한 (Security & XSS) 전용 테스트")
    class ServiceSecurityTest {

        @Test
        @DisplayName("[보안/XSS] 옷 생성 시 이름에 악성 스크립트가 포함되어 있으면 안전한 HTML 코드로 치환(Escaping)한다.")
        void prevent_XSS_On_Create() {
            // given
            UUID loginUserId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID(); // ID 고정
            ClothesAttributeDto attrDto = new ClothesAttributeDto(attributeId, "COTTON");

            String maliciousName = "<script>alert('해킹')</script> 청 자켓";
            ClothesCreateRequest request = new ClothesCreateRequest(loginUserId, maliciousName, ClothesType.OUTER, List.of(attrDto));

            User mockUser = mock(User.class);
            given(userRepository.findById(loginUserId)).willReturn(Optional.of(mockUser));

            Attribute mockAttribute = mock(Attribute.class);
            given(mockAttribute.getId()).willReturn(attributeId);
            given(mockAttribute.getName()).willReturn("소재");

            SelectableValue mockSelectableValue = mock(SelectableValue.class);
            given(mockSelectableValue.getAttribute()).willReturn(mockAttribute);
            given(mockSelectableValue.getType()).willReturn("COTTON");

            given(attributeRepository.findAllById(any())).willReturn(List.of(mockAttribute));
            given(selectableValueRepository.findAllByAttributeIdIn(any())).willReturn(List.of(mockSelectableValue));

            Clothes mockSavedClothes = Clothes.createWithImage("&lt;script&gt;alert(&#39;해킹&#39;)&lt;/script&gt; 청 자켓", ClothesType.OUTER, mockUser, null);
            given(clothesRepository.save(any(Clothes.class))).willReturn(mockSavedClothes);

            // when
            ClothesDto result = clothesService.createClothes(request, null, loginUserId);

            // then
            assertThat(result.name()).isEqualTo("&lt;script&gt;alert(&#39;해킹&#39;)&lt;/script&gt; 청 자켓");
        }

        @Test
        @DisplayName("[보안/XSS] 옷 수정 시 이름에 악성 스크립트가 포함되어 있으면 안전한 HTML 코드로 치환(Escaping)한다.")
        void prevent_XSS_On_Update() {
            // given
            UUID loginUserId = UUID.randomUUID();
            UUID attributeId = UUID.randomUUID();
            ClothesAttributeDto attrDto = new ClothesAttributeDto(attributeId, "COTTON");

            // 해커의 XSS 스크립트 공격
            String maliciousName = "<script>alert('해킹')</script> 청 자켓";
            ClothesCreateRequest request = new ClothesCreateRequest(loginUserId, maliciousName, ClothesType.OUTER, List.of(attrDto));

            User mockUser = mock(User.class);
            given(userRepository.findById(loginUserId)).willReturn(Optional.of(mockUser));

            // Attribute/SelectableValue 모킹
            Attribute mockAttribute = mock(Attribute.class);
            given(mockAttribute.getId()).willReturn(attributeId);
            given(mockAttribute.getName()).willReturn("소재");

            SelectableValue mockSelectableValue = mock(SelectableValue.class);
            given(mockSelectableValue.getAttribute()).willReturn(mockAttribute);
            given(mockSelectableValue.getType()).willReturn("COTTON"); //

            given(attributeRepository.findAllById(anyList())).willReturn(List.of(mockAttribute));
            given(selectableValueRepository.findAllByAttributeIdIn(anyList())).willReturn(List.of(mockSelectableValue));

            // 이스케이프된 문자열로 세팅된 옷 객체
            Clothes mockSavedClothes = Clothes.createWithImage("&lt;script&gt;alert(&#39;해킹&#39;)&lt;/script&gt; 청 자켓", ClothesType.OUTER, mockUser, null);
            given(clothesRepository.save(any(Clothes.class))).willReturn(mockSavedClothes);

            // when
            ClothesDto result = clothesService.createClothes(request, null, loginUserId);

            // then: XSS 무력화 증명
            assertThat(result.name()).isEqualTo("&lt;script&gt;alert(&#39;해킹&#39;)&lt;/script&gt; 청 자켓");
        }

        @Test
        @DisplayName("[보안/인가] 옷 생성 시 로그인한 본인이 아닌 타인 명의(ownerId)를 사용하려 하면 AUTH_FORBIDDEN 발생")
        void createClothes_Fail_Forbidden_IdSpoofing() {
            // given
            UUID loginUserId = UUID.randomUUID(); // 로그인 유저
            UUID maliciousTargetId = UUID.randomUUID(); // 타겟의 ID

            ClothesCreateRequest request = new ClothesCreateRequest(maliciousTargetId, "훔친 셔츠", ClothesType.TOP, List.of());

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.createClothes(request, null, loginUserId)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN);
            then(clothesRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[보안/인가] 타인의 옷을 수정하려고 시도하면 AUTH_FORBIDDEN 발생")
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

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothesService.updateClothes(clothesId, maliciousUserId, request, null)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN);
        }

        @Test
        @DisplayName("[보안/인가] 타인의 옷을 삭제하려고 시도하면 AUTH_FORBIDDEN 발생")
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

        @Test
        @DisplayName("[보안/무결성] 옷 목록 조회 시, 클라이언트가 타인의 ID를 넘겨도 서버가 강제로 로그인 유저 ID로 덮어씌운다.")
        void getClothesList_Overwrite_MaliciousOwnerId() {
            // given
            UUID loginUserId = UUID.randomUUID();
            UUID maliciousTargetId = UUID.randomUUID();

            // 남의 ID를 훔쳐보려는 파라미터 조작
            ClothesDtoCursorRequest hackedRequest = new ClothesDtoCursorRequest(
                    null, null, 20, null, maliciousTargetId.toString(), SortBy.createdAt, SortDirection.DESCENDING
            );

            ClothesDtoCursorResponse mockResponse = new ClothesDtoCursorResponse(
                    List.of(), null, null, false, 0L, SortBy.createdAt, SortDirection.DESCENDING
            );
            given(clothesRepository.findClothesByCursor(any(ClothesDtoCursorRequest.class))).willReturn(mockResponse);

            // when
            clothesService.getClothesList(hackedRequest, loginUserId);

            // then: ArgumentCaptor를 사용하여 덮어쓰기 여부 확인
            ArgumentCaptor<ClothesDtoCursorRequest> requestCaptor = ArgumentCaptor.forClass(ClothesDtoCursorRequest.class);
            then(clothesRepository).should(times(1)).findClothesByCursor(requestCaptor.capture());

            ClothesDtoCursorRequest securedRequest = requestCaptor.getValue();

            assertThat(securedRequest.ownerId()).isEqualTo(loginUserId.toString());
            assertThat(securedRequest.ownerId()).isNotEqualTo(maliciousTargetId.toString());
        }
    }
}