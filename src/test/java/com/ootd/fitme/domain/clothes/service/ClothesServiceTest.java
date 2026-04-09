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
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.infrastructure.storage.image.ImageStorage;
import com.ootd.fitme.infrastructure.storage.log.LogStorage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("local")
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@DisplayName("ClothesService 통합 테스트")
class ClothesServiceTest {

    @Autowired private ClothesServiceImpl clothesService;
    @Autowired private ClothesRepository clothesRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AttributeRepository attributeRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private EntityManager em;

    @MockitoBean
    private ImageStorage imageStorage;

    private User testUser;
    private Attribute colorAttribute;
    private Attribute materialAttribute;

    @BeforeEach
    void setUp() {
        String uniqueEmail = "integration+" + UUID.randomUUID() + "@test.com";
        testUser = User.create(uniqueEmail, "pass123");
        userRepository.save(testUser);

        colorAttribute = Attribute.create("색상");
        colorAttribute.addValues(List.of("빨강", "파랑", "검정"));
        attributeRepository.save(colorAttribute);

        materialAttribute = Attribute.create("소재");
        materialAttribute.addValues(List.of("면", "데님"));
        attributeRepository.save(materialAttribute);

        flushAndClear();
    }

    @AfterEach
    void tearDown() {
        clothesRepository.deleteAll();
        attributeRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("비즈니스 로직 테스트 (Create, Read, Update, Delete)")
    class BusinessLogicTest {

        @Test
        @DisplayName("옷 생성 성공 (이미지 포함)")
        void createClothes_Success() {
            // given
            ClothesAttributeDto colorDto = new ClothesAttributeDto(colorAttribute.getId(), "파랑");
            ClothesAttributeDto materialDto = new ClothesAttributeDto(materialAttribute.getId(), "면");

            ClothesCreateRequest request = new ClothesCreateRequest(
                    testUser.getId(), "파란 면 셔츠", ClothesType.TOP, List.of(colorDto, materialDto)
            );

            MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "img".getBytes());
            String expectedUrl = "https://cdn.fitme.com/test.jpg";
            given(imageStorage.upload(any(), anyString())).willReturn(expectedUrl);

            // when
            ClothesDto result = clothesService.createClothes(request, mockImage, testUser.getId());
            flushAndClear();

            // then: 실제 DB에 저장된 데이터 검증
            Clothes savedClothes = clothesRepository.findByIdWithDetails(result.id()).orElseThrow();
            assertThat(savedClothes.getName()).isEqualTo("파란 면 셔츠");
            assertThat(savedClothes.getImageUrl()).isEqualTo(expectedUrl); // 이미지 URL 확인

            // 연관된 속성(Cascade) 저장 여부 검증
            List<ClothesAttribute> dbAttributes = savedClothes.getAttributes();
            assertThat(dbAttributes).hasSize(2);
            assertThat(dbAttributes).extracting(attr ->
                    attr.getClothesAttributeSelectableValue().getSelectableValue().getType()
            ).containsExactlyInAnyOrder("파랑", "면");

            verify(imageStorage, times(1)).upload(mockImage, "clothes");
        }

        @Test
        @DisplayName("존재하지 않는 옵션 선택 시 예외 발생")
        void createClothes_Fail_InvalidOption() {
            // given
            ClothesAttributeDto invalidColorDto = new ClothesAttributeDto(colorAttribute.getId(), "노랑");
            ClothesCreateRequest request = new ClothesCreateRequest(
                    testUser.getId(), "노란 셔츠", ClothesType.TOP, List.of(invalidColorDto)
            );

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(request, null, testUser.getId()))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OPTION_NOT_FOUND);
        }

        @Test
        @DisplayName("옷 수정 성공 (새 이미지 업로드 및 정보 수정)")
        void updateClothes_Success() {
            // given: 기존 옷 생성
            String oldUrl = "https://cdn.fitme.com/old.jpg";
            ClothesCreateRequest createReq = new ClothesCreateRequest(
                    testUser.getId(), "기존 셔츠", ClothesType.TOP,
                    List.of(new ClothesAttributeDto(colorAttribute.getId(), "빨강"))
            );
            given(imageStorage.upload(any(), anyString())).willReturn(oldUrl);
            ClothesDto createdDto = clothesService.createClothes(createReq, new MockMultipartFile("img", new byte[0]), testUser.getId());
            flushAndClear();

            // when: 옷 정보, 속성, 이미지 수정
            String newUrl = "https://cdn.fitme.com/new.jpg";
            MockMultipartFile mockNewImage = new MockMultipartFile("image", "new.jpg", "image/jpeg", "new_img".getBytes());
            given(imageStorage.upload(mockNewImage, "clothes")).willReturn(newUrl);

            ClothesUpdateRequest updateReq = new ClothesUpdateRequest(
                    "수정된 셔츠", ClothesType.TOP,
                    List.of(new ClothesAttributeDto(colorAttribute.getId(), "파랑"))
            );
            clothesService.updateClothes(createdDto.id(), testUser.getId(), updateReq, mockNewImage);
            flushAndClear();

            // then: DB 반영 확인
            Clothes updatedClothes = clothesRepository.findById(createdDto.id()).orElseThrow();
            assertThat(updatedClothes.getName()).isEqualTo("수정된 셔츠");
            assertThat(updatedClothes.getImageUrl()).isEqualTo(newUrl);
            assertThat(updatedClothes.getAttributes()).hasSize(1);
            assertThat(updatedClothes.getAttributes().get(0).getClothesAttributeSelectableValue().getSelectableValue().getType()).isEqualTo("파랑");
        }

        @Test
        @DisplayName("옷 삭제 성공 및 연관 데이터(고아 객체) 삭제 확인")
        void deleteClothes_Success() {
            // given
            ClothesCreateRequest createReq = new ClothesCreateRequest(
                    testUser.getId(), "삭제될 자켓", ClothesType.OUTER, List.of()
            );
            ClothesDto createdDto = clothesService.createClothes(createReq, null, testUser.getId());
            flushAndClear();

            // when
            clothesService.deleteClothes(createdDto.id(), testUser.getId());
            flushAndClear();

            // then
            assertThat(clothesRepository.findById(createdDto.id())).isEmpty();

            Long attributeCount = em.createQuery(
                            "SELECT COUNT(ca) FROM ClothesAttribute ca WHERE ca.clothes.id = :id", Long.class)
                    .setParameter("id", createdDto.id())
                    .getSingleResult();
            assertThat(attributeCount).isEqualTo(0L);
        }

        @Test
        @DisplayName("목록 페이징 조회 성공")
        void getClothesList_Success() {
            // given: 테스트용 옷 3개 생성 (순서 보장)
            for (int i = 1; i <= 3; i++) {
                ClothesCreateRequest req = new ClothesCreateRequest(testUser.getId(), "옷_" + i, ClothesType.TOP, List.of());
                clothesService.createClothes(req, null, testUser.getId());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            flushAndClear();

            // when: 첫 페이지 (Limit 2)
            ClothesDtoCursorRequest firstRequest = new ClothesDtoCursorRequest(
                    null, null, 2, null, testUser.getId().toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );
            ClothesDtoCursorResponse firstResponse = clothesService.getClothesList(firstRequest, testUser.getId());

            // then
            assertThat(firstResponse.data()).hasSize(2);
            assertThat(firstResponse.data().get(0).name()).isEqualTo("옷_3");
            assertThat(firstResponse.hasNext()).isTrue();

            // when: 두 번째 페이지
            ClothesDtoCursorRequest secondRequest = new ClothesDtoCursorRequest(
                    firstResponse.nextCursor(), firstResponse.nextIdAfter(), 2, null, testUser.getId().toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );
            ClothesDtoCursorResponse secondResponse = clothesService.getClothesList(secondRequest, testUser.getId());

            // then
            assertThat(secondResponse.data()).hasSize(1);
            assertThat(secondResponse.data().get(0).name()).isEqualTo("옷_1");
            assertThat(secondResponse.hasNext()).isFalse();
        }

    }

    @Nested
    @DisplayName("보안 및 인가 (Security & Auth) 테스트")
    class SecurityTest {

        @Test
        @DisplayName("생성 시 XSS 스크립트 치환 확인")
        void prevent_XSS_On_Create() {
            // given
            String maliciousName = "<script>alert(1)</script> 셔츠";
            ClothesCreateRequest request = new ClothesCreateRequest(testUser.getId(), maliciousName, ClothesType.TOP, List.of());

            // when
            ClothesDto result = clothesService.createClothes(request, null, testUser.getId());
            flushAndClear();

            // then
            Clothes savedClothes = clothesRepository.findById(result.id()).orElseThrow();
            assertThat(savedClothes.getName()).isEqualTo("&lt;script&gt;alert(1)&lt;/script&gt; 셔츠");
        }

        @Test
        @DisplayName("수정 시 XSS 스크립트 치환 확인")
        void prevent_XSS_On_Update() {
            // given
            ClothesCreateRequest createReq = new ClothesCreateRequest(testUser.getId(), "일반 셔츠", ClothesType.TOP, List.of());
            ClothesDto createdDto = clothesService.createClothes(createReq, null, testUser.getId());
            flushAndClear();

            String maliciousName = "<img src=x onerror=alert(1)> 셔츠";
            ClothesUpdateRequest updateReq = new ClothesUpdateRequest(maliciousName, ClothesType.TOP, List.of());

            // when
            clothesService.updateClothes(createdDto.id(), testUser.getId(), updateReq, null);
            flushAndClear();

            // then
            Clothes updatedClothes = clothesRepository.findById(createdDto.id()).orElseThrow();
            assertThat(updatedClothes.getName()).isEqualTo("&lt;img src=x onerror=alert(1)&gt; 셔츠");
        }

        @Test
        @DisplayName("타인 명의로 생성 시도 시 예외 발생")
        void createClothes_Fail_Forbidden() {
            // given
            User otherUser = userRepository.save(User.create("other@test.com", "pass"));
            ClothesCreateRequest request = new ClothesCreateRequest(otherUser.getId(), "타인의 셔츠", ClothesType.TOP, List.of());

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(request, null, testUser.getId()))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
        }

        @Test
        @DisplayName("타인의 옷 수정 시도 시 예외 발생")
        void updateClothes_Fail_Forbidden() {
            // given
            User otherUser = userRepository.save(User.create("other@test.com", "pass"));
            ClothesCreateRequest createReq = new ClothesCreateRequest(otherUser.getId(), "타인의 옷", ClothesType.TOP, List.of());
            ClothesDto createdDto = clothesService.createClothes(createReq, null, otherUser.getId());
            flushAndClear();

            ClothesUpdateRequest updateReq = new ClothesUpdateRequest("수정 시도", ClothesType.TOP, List.of());

            // when & then
            assertThatThrownBy(() -> clothesService.updateClothes(createdDto.id(), testUser.getId(), updateReq, null))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
        }

        @Test
        @DisplayName("타인의 옷 삭제 시도 시 예외 발생")
        void deleteClothes_Fail_Forbidden() {
            // given
            User otherUser = userRepository.save(User.create("other@test.com", "pass"));
            ClothesCreateRequest createReq = new ClothesCreateRequest(otherUser.getId(), "타인의 옷", ClothesType.TOP, List.of());
            ClothesDto createdDto = clothesService.createClothes(createReq, null, otherUser.getId());
            flushAndClear();

            // when & then
            assertThatThrownBy(() -> clothesService.deleteClothes(createdDto.id(), testUser.getId()))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
        }

        @Test
        @DisplayName("목록 조회 시 악의적인 ownerId를 넘겨도 로그인 유저의 ID로 덮어씌움")
        void getClothesList_Overwrite_OwnerId() {
            // given
            ClothesCreateRequest myReq = new ClothesCreateRequest(testUser.getId(), "내 옷", ClothesType.TOP, List.of());
            clothesService.createClothes(myReq, null, testUser.getId());

            User otherUser = userRepository.save(User.create("other@test.com", "pass"));
            ClothesCreateRequest otherReq = new ClothesCreateRequest(otherUser.getId(), "타인 옷", ClothesType.TOP, List.of());
            clothesService.createClothes(otherReq, null, otherUser.getId());
            flushAndClear();

            ClothesDtoCursorRequest hackedRequest = new ClothesDtoCursorRequest(
                    null, null, 20, null, otherUser.getId().toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );

            // when
            ClothesDtoCursorResponse response = clothesService.getClothesList(hackedRequest, testUser.getId());

            // then
            assertThat(response.data()).hasSize(1);
            assertThat(response.data().get(0).name()).isEqualTo("내 옷");
        }
    }
}