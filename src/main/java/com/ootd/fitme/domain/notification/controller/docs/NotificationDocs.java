package com.ootd.fitme.domain.notification.controller.docs;

import com.ootd.fitme.domain.notification.dto.request.NotificationPageQueryRequest;
import com.ootd.fitme.domain.notification.dto.response.NotificationPageResponse;
import com.ootd.fitme.domain.notification.exception.NotificationBadRequestException;
import com.ootd.fitme.domain.notification.exception.NotificationException;
import com.ootd.fitme.global.exception.ErrorResponse;
import com.ootd.fitme.global.exception.FitmeException;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(
        name = "알림 관리",
        description = "알림 관련 API"
)
public interface NotificationDocs {

    @Operation(
            summary = "알림 목록 조회",
            description = "현재 로그인한 사용자의 알림 목록을 커서 기반으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationPageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 값",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<NotificationPageResponse> getNotifications(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,

            @Valid
            @ParameterObject
            @ModelAttribute
            NotificationPageQueryRequest query
    );

    @Operation(
            summary = "알림 삭제",
            description = "현재 로그인한 사용자의 알림 1건을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "알림 삭제 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 값",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<Void> deleteNotification(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal,

            @Parameter(
                    description = "삭제할 알림 ID"
            )
            @PathVariable UUID notificationId
    );

}
