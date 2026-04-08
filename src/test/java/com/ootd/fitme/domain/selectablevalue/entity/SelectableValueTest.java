package com.ootd.fitme.domain.selectablevalue.entity;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SelectableValue 엔티티 단위 테스트")
class SelectableValueTest {

    private Attribute mockAttribute;

    @BeforeEach
    void setUp() {
        mockAttribute = Attribute.create("임시속성");
    }

    @Nested
    @DisplayName("create() 메서드로 옵션을 생성할 때")
    class Describe_create {

        @Test
        @DisplayName("정상적인 값과 순서가 주어지면 객체가 성공적으로 생성된다.")
        void it_creates_instance_with_order() {
            // given
            String type = "XL";
            int displayOrder = 2;

            // when
            SelectableValue option = SelectableValue.create(type, displayOrder, mockAttribute);

            // then
            assertThat(option.getType()).isEqualTo("XL");
            assertThat(option.getDisplayOrder()).isEqualTo(2);
            assertThat(option.getAttribute()).isEqualTo(mockAttribute);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "  ", "\t", "\n"})
        @DisplayName("타입 문자열이 공백이면 INVALID_INPUT_VALUE 예외를 던진다.")
        void it_throws_exception_when_type_is_blank(String invalidType) {
            assertThatThrownBy(() -> SelectableValue.create(invalidType, 0, mockAttribute))
                    .isInstanceOf(FitmeException.class)
                    .hasMessage(ErrorCode.SELECTABLE_VALUE_INVALID.getMessage());
        }

        @Test
        @DisplayName("타입 문자열이 null이면 INVALID_INPUT_VALUE 예외를 던진다.")
        void it_throws_exception_when_type_is_null() {
            assertThatThrownBy(() -> SelectableValue.create(null, 0, mockAttribute))
                    .isInstanceOf(FitmeException.class)
                    .hasMessage(ErrorCode.SELECTABLE_VALUE_INVALID.getMessage());
        }
    }
}