package qwerty.chaekit.controller.member.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import qwerty.chaekit.dto.member.UserInfoResponse;
import qwerty.chaekit.dto.page.PageResponse;
import qwerty.chaekit.global.response.ApiSuccessResponse;
import qwerty.chaekit.service.member.admin.AdminService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @Operation(
            summary = "유저 목록 조회",
            description = "모든 유저 목록을 확인할 수 있습니다."
    )
    @GetMapping("/users")
    public ApiSuccessResponse<PageResponse<UserInfoResponse>> fetchUsers(@ParameterObject Pageable pageable) {
        return ApiSuccessResponse.of(adminService.getUsers(pageable));
    }
}
