package qwerty.chaekit.controller.ebook;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import qwerty.chaekit.dto.ebook.EbookFetchResponse;
import qwerty.chaekit.dto.ebook.shelf.EbookRegisterResponse;
import qwerty.chaekit.dto.page.PageResponse;
import qwerty.chaekit.global.response.ApiSuccessResponse;
import qwerty.chaekit.global.security.resolver.Login;
import qwerty.chaekit.global.security.resolver.UserToken;
import qwerty.chaekit.service.ebook.EbookShelfService;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class EbookShelfController {
    public final EbookShelfService ebookShelfService;

    @PostMapping("/{bookId}/register")
    @Operation(summary = "전자책 서재 등록", description = "전자책을 내 서재에 등록합니다.")
    public ApiSuccessResponse<EbookRegisterResponse> registerEbook(
            @Parameter(hidden = true) @Login UserToken userToken,
            @Parameter(description = "전자책 ID") @PathVariable Long bookId
    ) {
        return ApiSuccessResponse.of(ebookShelfService.registerEbook(bookId, userToken.userId()));
    }

    @GetMapping("/my")
    @Operation(summary = "내 전자책 목록 조회", description = "내가 구매한 전자책 목록을 페이지네이션하여 조회합니다.")
    public ApiSuccessResponse<PageResponse<EbookFetchResponse>> getMyBooks(
            @Parameter(hidden = true) @Login UserToken userToken,
            @Parameter(description = "페이지네이션 정보") @ParameterObject Pageable pageable
    ) {
        return ApiSuccessResponse.of(ebookShelfService.getMyBooks(userToken.userId(), pageable));
    }


}
