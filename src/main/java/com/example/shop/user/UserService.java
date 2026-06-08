package com.example.shop.user;

import com.example.shop.common.BusinessException;
import com.example.shop.common.Hashing;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserAccount register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already exists");
        }
        UserAccount user = new UserAccount();
        user.setUsername(request.username());
        user.setPasswordHash(Hashing.sha256(request.password()));
        user.setEmail(request.email());
        user.setPhone(request.phone());
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Invalid username or password"));
        if (!user.getPasswordHash().equals(Hashing.sha256(request.password()))) {
            throw new BusinessException("Invalid username or password");
        }
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole(), "Use X-User-Id=" + user.getId());
    }

    @Transactional
    public UserAccount updateProfile(Long userId, UpdateProfileRequest request) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setEmail(request.email());
        user.setPhone(request.phone());
        return user;
    }

    public record RegisterRequest(String username, String password, String email, String phone) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record UpdateProfileRequest(String email, String phone) {
    }

    public record LoginResponse(Long userId, String username, UserRole role, String demoToken) {
    }
}
