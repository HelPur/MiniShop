package com.example.shop.user;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.CurrentUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<UserAccount> register(@RequestBody UserService.RegisterRequest request) {
        return ApiResponse.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<UserService.LoginResponse> login(@RequestBody UserService.LoginRequest request) {
        return ApiResponse.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserAccount> me() {
        return ApiResponse.ok(CurrentUserContext.getRequired());
    }

    @PatchMapping("/me")
    public ApiResponse<UserAccount> updateProfile(@RequestBody UserService.UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(CurrentUserContext.getRequired().getId(), request));
    }
}
