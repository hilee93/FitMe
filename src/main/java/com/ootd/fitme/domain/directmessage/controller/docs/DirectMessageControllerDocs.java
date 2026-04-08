package com.ootd.fitme.domain.directmessage.controller.docs;

import com.ootd.fitme.domain.directmessage.dto.request.DirectMessageSearchCondition;
import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "DirectMessage", description = "DirectMessage API")
public interface DirectMessageControllerDocs {

    @Operation(summary = "DM 목록 조회", description = "DM 목록 조회 API")
    @ApiResponse(responseCode = "200", description = "DM 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "DM 목록 조회 실패")
    ResponseEntity<DirectMessageDtoCursorResponse> getDirectMessages(
            @Parameter(description = "조회할 사용자의 UUID", required = true) UUID userId,
            DirectMessageSearchCondition condition);
}
