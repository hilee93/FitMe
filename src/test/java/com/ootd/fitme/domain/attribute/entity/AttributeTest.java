package com.ootd.fitme.domain.attribute.entity;

import com.ootd.fitme.domain.attribute.exception.AttributeException;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Attribute 엔티티 단위 테스트")
class AttributeTest {

    @Nested
    @DisplayName("create() 시 속성 이름 검증:")
    class Describe_validation {
        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        @DisplayName("이름이 공백이면 ATTRIBUTE_NAME_INVALID 예외를 던진다.")
        void it_throws_exception(String invalidName) {
            assertThatThrownBy(() -> Attribute.create(invalidName))
                    .isInstanceOf(AttributeException.class)
                    .hasMessage(ErrorCode.ATTRIBUTE_NAME_INVALID.getMessage());
        }
    }

    @Nested
    @DisplayName("updateAttribute() 메서드는")
    class Describe_updateAttribute {

        @Test
        @DisplayName("이름을 수정하고, 기존 옵션의 객체를 유지한 채 병합(추가/삭제/순서변경)한다.")
        void it_updates_name_and_merges_options() {
            // given
            Attribute attribute = Attribute.create("핏");

            // 인덱스: 0=오버핏, 1=레귤러핏, 2=슬림핏
            attribute.addValues(List.of("오버핏", "레귤러핏", "슬림핏"));

            SelectableValue originalRegularFit = attribute.getSelectableValues().get(1);

            // when (실행)
            attribute.updateAttribute("수정된핏", List.of("레귤러핏", "크롭핏"));

            // then (검증)
            assertThat(attribute.getName()).isEqualTo("수정된핏");
            assertThat(attribute.getSelectableValues()).hasSize(2);

            SelectableValue updatedRegularFit = attribute.getSelectableValues().get(0);
            assertThat(updatedRegularFit.getType()).isEqualTo("레귤러핏");
            assertThat(updatedRegularFit.getDisplayOrder()).isEqualTo(0);
            assertThat(updatedRegularFit).isSameAs(originalRegularFit);

            SelectableValue newCropFit = attribute.getSelectableValues().get(1);
            assertThat(newCropFit.getType()).isEqualTo("크롭핏");
            assertThat(newCropFit.getDisplayOrder()).isEqualTo(1);
        }
    }
}