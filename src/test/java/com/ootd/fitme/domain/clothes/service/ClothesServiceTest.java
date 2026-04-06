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
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import com.ootd.fitme.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@DisplayName("ClothesService CRUD 통합 테스트")
class ClothesServiceTest {

    @Autowired
    private ClothesServiceImpl clothesService;
    @Autowired
    private ClothesRepository clothesRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AttributeRepository attributeRepository;
    @Autowired
    private EntityManager em;

    private User testUser;
    private Attribute colorAttribute;
    private Attribute materialAttribute;

    @BeforeEach
    void setUp() {
        testUser = User.create("integration@test.com", "pass123");
        userRepository.save(testUser);

        colorAttribute = Attribute.create("색상");
        colorAttribute.addValues(List.of("빨강", "파랑", "검정"));
        attributeRepository.save(colorAttribute);

        materialAttribute = Attribute.create("소재");
        materialAttribute.addValues(List.of("면", "데님"));
        attributeRepository.save(materialAttribute);

        flushAndClear();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("옷 생성 (Create) 통합 테스트")
    class CreateClothesTest {

        @Test
        @DisplayName("[성공] 올바른 요청 시 실제 DB에 옷과 속성 매핑 데이터가 정상적으로 INSERT 된다.")
        void createClothes_Success_DB_Insert() {
            // given
            ClothesAttributeDto colorDto = new ClothesAttributeDto(colorAttribute.getId(), "파랑");
            ClothesAttributeDto materialDto = new ClothesAttributeDto(materialAttribute.getId(), "면");

            ClothesCreateRequest request = new ClothesCreateRequest(
                    testUser.getId(), "파란 면 셔츠", ClothesType.TOP, List.of(colorDto, materialDto)
            );

            // when
            ClothesDto result = clothesService.createClothes(request, null);
            flushAndClear(); // 실제 DB 반영 확인을 위해 플러시

            // then
            Clothes savedClothes = clothesRepository.findByIdWithDetails(result.Id()).orElseThrow();
            assertThat(savedClothes.getName()).isEqualTo("파란 면 셔츠");
            assertThat(savedClothes.getClothesType()).isEqualTo(ClothesType.TOP);

            // 영속성 전이(Cascade)를 통해 자식 엔티티(Attributes)가 잘 저장되었는지 DB 단에서 확인
            List<ClothesAttribute> dbAttributes = savedClothes.getAttributes();
            assertThat(dbAttributes).hasSize(2);
            assertThat(dbAttributes).extracting(attr ->
                    attr.getClothesAttributeSelectableValue().getSelectableValue().getType()
            ).containsExactlyInAnyOrder("파랑", "면");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 유저 ID로 생성 시 CLOTHES_OWNER_NOT_FOUND 예외를 던진다.")
        void createClothes_Fail_InvalidUser() {
            // given
            UUID invalidUserId = UUID.randomUUID();
            ClothesCreateRequest request = new ClothesCreateRequest(
                    invalidUserId, "유령 셔츠", ClothesType.TOP, List.of()
            );

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(request, null))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOTHES_OWNER_NOT_FOUND);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 옵션(예: 노랑)을 선택하면 DB 저장 전 OPTION_NOT_FOUND 예외를 던진다.")
        void createClothes_Fail_InvalidOption() {
            // given (등록되지 않은 옵션 '노랑' 요청)
            ClothesAttributeDto invalidColorDto = new ClothesAttributeDto(colorAttribute.getId(), "노랑");
            ClothesCreateRequest request = new ClothesCreateRequest(
                    testUser.getId(), "노란 셔츠", ClothesType.TOP, List.of(invalidColorDto)
            );

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(request, null))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OPTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("옷 목록 조회 (Read/Pagination) 통합 테스트")
    class GetClothesListTest {

        @Test
        @DisplayName("[성공] 본인의 옷만 정렬 및 Limit 조건에 맞춰 정상적으로 페이징 조회된다.")
        void getClothesList_Success_Pagination() {
            // given
            for (int i = 1; i <= 3; i++) {
                ClothesCreateRequest req = new ClothesCreateRequest(
                        testUser.getId(), "옷_" + i, ClothesType.TOP, List.of()
                );
                clothesService.createClothes(req, null);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            User otherUser = userRepository.save(User.create("other@test.com", "pass"));
            clothesService.createClothes(
                    new ClothesCreateRequest(otherUser.getId(), "타인의 옷", ClothesType.TOP, List.of()), null
            );
            flushAndClear();

            // when: 첫 페이지 조회 (Limit 2, 이름 기준 내림차순 정렬)
            ClothesDtoCursorRequest firstRequest = new ClothesDtoCursorRequest(
                    null, null, 2, null, testUser.getId().toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );
            ClothesDtoCursorResponse firstResponse = clothesService.getClothesList(firstRequest, testUser.getId());

            // then: 1. 본인 옷 중 이름 역순(옷_3, 옷_2) 2개만 조회되어야 함
            assertThat(firstResponse.data()).hasSize(2);
            assertThat(firstResponse.data().get(0).name()).isEqualTo("옷_3");
            assertThat(firstResponse.data().get(1).name()).isEqualTo("옷_2");
            assertThat(firstResponse.hasNext()).isTrue();
            assertThat(firstResponse.totalCount()).isEqualTo(3L);

            // when: 두 번째 페이지 조회 (마지막으로 읽은 '옷_2'의 커서를 기반으로 요청)
            ClothesDtoCursorRequest secondRequest = new ClothesDtoCursorRequest(
                    firstResponse.nextCursor(), firstResponse.nextIdAfter(), 2, null, testUser.getId().toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );
            ClothesDtoCursorResponse secondResponse = clothesService.getClothesList(secondRequest, testUser.getId());

            // then: 2. 남은 '옷_1' 1개만 조회되고 hasNext는 false가 되어야 함
            assertThat(secondResponse.data()).hasSize(1);
            assertThat(secondResponse.data().get(0).name()).isEqualTo("옷_1");
            assertThat(secondResponse.hasNext()).isFalse();
        }

        @Test
        @DisplayName("[실패] 커서 정보 중 일부(idAfter)만 누락된 상태로 요청 시 INVALID_INPUT_VALUE 예외를 던진다.")
        void getClothesList_Fail_IncompleteCursor() {
            // given (cursor는 있지만 idAfter가 없는 비정상 요청)
            ClothesDtoCursorRequest request = new ClothesDtoCursorRequest(
                    "cursorData", null, 20, null, testUser.getId().toString(),
                    SortBy.createdAt, SortDirection.DESCENDING
            );

            // when & then
            assertThatThrownBy(() -> clothesService.getClothesList(request, testUser.getId()))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("옷 수정 (Update) 통합 테스트")
    class UpdateClothesTest {

        @Test
        @DisplayName("[성공] 기존 속성을 변경하면 DB에서 매핑이 정상 업데이트되며, 본인 옷만 수정 가능하다.")
        void updateClothes_Success_DB_Merge() {
            // given
            ClothesCreateRequest createReq = new ClothesCreateRequest(
                    testUser.getId(), "기존 셔츠", ClothesType.TOP,
                    List.of(new ClothesAttributeDto(colorAttribute.getId(), "빨강"))
            );
            ClothesDto createdDto = clothesService.createClothes(createReq, null);
            flushAndClear();

            // when
            ClothesUpdateRequest updateReq = new ClothesUpdateRequest(
                    "수정된 셔츠", ClothesType.TOP,
                    List.of(new ClothesAttributeDto(colorAttribute.getId(), "파랑"))
            );
            clothesService.updateClothes(createdDto.Id(), testUser.getId(), updateReq, null);
            flushAndClear();

            // then
            Clothes updatedClothes = clothesRepository.findById(createdDto.Id()).orElseThrow();
            assertThat(updatedClothes.getName()).isEqualTo("수정된 셔츠");
        }

        @Test
        @DisplayName("[실패] 타인의 옷을 수정하려고 하면 DB 반영 없이 예외가 발생한다.")
        void updateClothes_Fail_Forbidden() {
            // given: 타인(otherUser)의 옷 생성
            User otherUser = userRepository.save(User.create("other@test.com", "pass"));
            ClothesCreateRequest createReq = new ClothesCreateRequest(
                    otherUser.getId(), "타인의 셔츠", ClothesType.TOP, List.of()
            );
            ClothesDto createdDto = clothesService.createClothes(createReq, null);
            flushAndClear();

            ClothesUpdateRequest updateReq = new ClothesUpdateRequest("몰래 수정", ClothesType.BOTTOM, List.of());

            // when & then
            assertThatThrownBy(() -> clothesService.updateClothes(createdDto.Id(), testUser.getId(), updateReq, null))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("옷 삭제 (Delete) 통합 테스트")
    class DeleteClothesTest {

        @Test
        @DisplayName("[성공] 본인 옷 삭제 시, 벌크 연산을 통해 매핑 데이터까지 DB에서 완벽하게 삭제된다.")
        void deleteClothes_Success_BulkDelete() {
            // given
            ClothesCreateRequest createReq = new ClothesCreateRequest(
                    testUser.getId(), "삭제될 자켓", ClothesType.OUTER, List.of()
            );
            ClothesDto createdDto = clothesService.createClothes(createReq, null);
            flushAndClear();

            // when
            clothesService.deleteClothes(createdDto.Id(), testUser.getId());
            flushAndClear();

            // then
            assertThat(clothesRepository.findById(createdDto.Id())).isEmpty();
        }

        @Test
        @DisplayName("[실패] 타인의 옷을 삭제하려고 하면 예외가 발생한다.")
        void deleteClothes_Fail_Forbidden() {
            // given
            User otherUser = userRepository.save(User.create("other@test.com", "pass"));
            ClothesCreateRequest createReq = new ClothesCreateRequest(otherUser.getId(), "타인의 옷", ClothesType.TOP, List.of());
            ClothesDto createdDto = clothesService.createClothes(createReq, null);
            flushAndClear();

            // when & then
            assertThatThrownBy(() -> clothesService.deleteClothes(createdDto.Id(), testUser.getId()))
                    .isInstanceOf(ClothesException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
        }
    }
}