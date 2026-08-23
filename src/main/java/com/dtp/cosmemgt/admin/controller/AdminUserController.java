package com.dtp.cosmemgt.admin.controller;

import com.dtp.cosmemgt.admin.dto.request.UserCreationRequest;
import com.dtp.cosmemgt.admin.dto.request.UserUpdateRequest;
import com.dtp.cosmemgt.admin.dto.response.UserResponse;
import com.dtp.cosmemgt.admin.service.UserService;
import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminUserController {
    UserService userService;

    @PostMapping()
    ApiResponse<UserResponse> adminCreateUser(
            @RequestBody @Valid UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request, true))
                .build();
    }

    @GetMapping
    ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "ALL") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.getUsers(role, page, size))
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    // Thêm vào AdminUserController.java
    @PutMapping("/{userId}/toggle-status")
    public ApiResponse<Void> toggleUserStatus(@PathVariable String userId) {
        userService.toggleUserStatus(userId);
        return ApiResponse.<Void>builder()
                .message("Cập nhật trạng thái tài khoản thành công")
                .build();
    }


}
