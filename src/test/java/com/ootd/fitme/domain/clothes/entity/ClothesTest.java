package com.ootd.fitme.domain.clothes.entity;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.exception.ClothesException;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;

@DisplayName("Clothes 엔티티 단위 테스트")
class ClothesTest {

    @Nested
    @DisplayName("옷 생성 (Factory Method) 테스트")
    class CreateTest {

        @Test
        @DisplayName("성공: 이미지 없이 옷을 생성하면 필드들이 정상적으로 초기화된다.")
        void create() {
            // given
            String name = "기본 흰 티셔츠";
            ClothesType type = ClothesType.TOP;
            User mockUser = mock(User.class);

            // when
            Clothes clothes = Clothes.create(name, type, mockUser);

            // then
            assertThat(clothes.getName()).isEqualTo(name);
            assertThat(clothes.getClothesType()).isEqualTo(type);
            assertThat(clothes.getUser()).isEqualTo(mockUser);
            assertThat(clothes.getImageUrl()).isNull();
            assertThat(clothes.getAttributes()).isEmpty();
        }

        @Test
        @DisplayName("성공: 이미지와 함께 옷을 생성하면 imageUrl도 함께 저장된다.")
        void createWithImage() {
            // given
            String name = "청바지";
            ClothesType type = ClothesType.BOTTOM;
            User mockUser = mock(User.class);
            String imageUrl = "https://s3.bucket.com/image.png";

            // when
            Clothes clothes = Clothes.createWithImage(name, type, mockUser, imageUrl);

            // then
            assertThat(clothes.getImageUrl()).isEqualTo(imageUrl);
        }
    }

    @Nested
    @DisplayName("속성 관리 (Attributes) 로직 테스트")
    class AttributesTest {

        @Test
        @DisplayName("성공: replaceAttributes를 호출하면 기존 속성이 모두 지워지고 새로운 속성으로 대체된다.")
        void replaceAttributes() {
            // given
            Clothes clothes = Clothes.create("셔츠", ClothesType.TOP, mock(User.class));

            ClothesAttribute oldAttr = mock(ClothesAttribute.class);
            clothes.getAttributes().add(oldAttr);

            ClothesAttribute newAttr1 = mock(ClothesAttribute.class);
            ClothesAttribute newAttr2 = mock(ClothesAttribute.class);
            List<ClothesAttribute> newAttributes = List.of(newAttr1, newAttr2);

            // when
            clothes.replaceAttributes(newAttributes);

            // then
            assertThat(clothes.getAttributes()).hasSize(2);
            assertThat(clothes.getAttributes()).containsExactly(newAttr1, newAttr2);
            assertThat(clothes.getAttributes()).doesNotContain(oldAttr);
        }

        @Test
        @DisplayName("성공: updateAttributes는 기존 속성 삭제, 추가, 값 변경을 정확히 수행한다.")
        void updateAttributes() {
            // given
            Clothes clothes = Clothes.create("자켓", ClothesType.OUTER, mock(User.class));

            UUID attributeId1 = UUID.randomUUID();
            UUID attributeId2 = UUID.randomUUID();
            UUID attributeId3 = UUID.randomUUID();

            UUID oldValueId1 = UUID.randomUUID();
            UUID oldValueId2 = UUID.randomUUID();
            UUID newValueId1 = UUID.randomUUID();
            UUID newValueId3 = UUID.randomUUID();

            ClothesAttribute existingToUpdate = createMockAttribute(attributeId1, oldValueId1);
            ClothesAttribute existingToRemove = createMockAttribute(attributeId2, oldValueId2);

            clothes.getAttributes().addAll(new ArrayList<>(List.of(existingToUpdate, existingToRemove)));

            ClothesAttribute incomingNew = createMockAttribute(attributeId3, newValueId3);
            ClothesAttribute incomingUpdate = createMockAttribute(attributeId1, newValueId1);

            List<ClothesAttribute> incomingAttributes = List.of(incomingNew, incomingUpdate);

            // when
            clothes.updateAttributes(incomingAttributes);

            // then
            List<ClothesAttribute> resultAttributes = clothes.getAttributes();

            assertThat(resultAttributes).hasSize(2);

            // 삭제 검증
            assertThat(resultAttributes).doesNotContain(existingToRemove);

            // 추가 검증
            assertThat(resultAttributes).contains(incomingNew);

            var mockCasv = existingToUpdate.getClothesAttributeSelectableValue();
            var incomingOption = incomingUpdate.getClothesAttributeSelectableValue().getSelectableValue();

            verify(mockCasv, times(1)).changeSelectableValue(incomingOption);
        }

        @Nested
        @DisplayName("옷 생성 검증 (Fail-Fast) 테스트")
        class CreateValidationTest {

            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("실패: 옷 이름이 null이거나 공백이면 CLOTHES_NAME_INVALID 예외가 발생한다.")
            void create_Fail_NameIsBlank(String invalidName) {
                User mockUser = mock(User.class);

                assertThatThrownBy(() -> Clothes.create(invalidName, ClothesType.TOP, mockUser))
                        .isInstanceOf(ClothesException.class)
                        .hasMessage(ErrorCode.CLOTHES_NAME_INVALID.getMessage());
            }

            @Test
            @DisplayName("실패: 옷 타입이 null이면 CLOTHES_TYPE_INVALID 예외가 발생한다.")
            void create_Fail_TypeIsNull() {
                User mockUser = mock(User.class);

                assertThatThrownBy(() -> Clothes.create("셔츠", null, mockUser))
                        .isInstanceOf(ClothesException.class)
                        .hasMessage(ErrorCode.CLOTHES_TYPE_INVALID.getMessage());
            }

            @Test
            @DisplayName("실패: 유저(User) 객체가 null이면 CLOTHES_USER_INVALID 예외가 발생한다.")
            void create_Fail_UserIsNull() {
                assertThatThrownBy(() -> Clothes.create("셔츠", ClothesType.TOP, null))
                        .isInstanceOf(ClothesException.class)
                        .hasMessage(ErrorCode.CLOTHES_USER_INVALID.getMessage());
            }
        }

        @Nested
        @DisplayName("옷 기본 정보 수정 (updateClothesInfo) 테스트")
        class UpdateClothesInfoTest {

            @Test
            @DisplayName("성공: 유효한 값이 주어지면 이름, 타입, 이미지가 모두 정상적으로 수정된다.")
            void updateClothesInfo_Success() {
                // given
                Clothes clothes = Clothes.createWithImage("기존 셔츠", ClothesType.TOP, mock(User.class), "old-image.png");

                // when
                clothes.updateClothesInfo("수정된 셔츠", ClothesType.OUTER, "new-image.png");

                // then
                assertThat(clothes.getName()).isEqualTo("수정된 셔츠");
                assertThat(clothes.getClothesType()).isEqualTo(ClothesType.OUTER);
                assertThat(clothes.getImageUrl()).isEqualTo("new-image.png");
            }

            @Test
            @DisplayName("성공: 변경할 이름과 타입으로 null이 들어오면 기존의 정보를 안전하게 유지한다.")
            void updateClothesInfo_IgnoresNullNameAndType() {
                // given
                Clothes clothes = Clothes.createWithImage("기존 바지", ClothesType.BOTTOM, mock(User.class), "pants.png");

                // when
                clothes.updateClothesInfo(null, null, "pants.png");

                // then
                assertThat(clothes.getName()).isEqualTo("기존 바지");
                assertThat(clothes.getClothesType()).isEqualTo(ClothesType.BOTTOM);
            }

            @Test
            @DisplayName("성공: 새로운 이미지 URL이 기존과 다르게 null(또는 빈 값)로 들어오면 이미지가 삭제 처리된다.")
            void updateClothesInfo_DeleteImage() {
                // given
                Clothes clothes = Clothes.createWithImage("자켓", ClothesType.OUTER, mock(User.class), "jacket.png");

                // when
                clothes.updateClothesInfo("자켓", ClothesType.OUTER, null);

                // then
                assertThat(clothes.getImageUrl()).isNull();
            }
        }

        /**
         * 깊은 객체 탐색(get.get.get...)을 위한 편의용 Mock 생성 메서드 (UUID 타입 적용)
         */
        private ClothesAttribute createMockAttribute(UUID attributeId, UUID selectableValueId) {
            ClothesAttribute mockAttr = mock(ClothesAttribute.class, RETURNS_DEEP_STUBS);

            when(mockAttr.getAttribute().getId()).thenReturn(attributeId);

            when(mockAttr.getClothesAttributeSelectableValue().getSelectableValue().getId())
                    .thenReturn(selectableValueId);

            return mockAttr;
        }
    }

}