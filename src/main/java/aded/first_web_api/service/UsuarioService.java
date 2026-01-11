package aded.first_web_api.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aded.first_web_api.dto.UserCreateRequest;
import aded.first_web_api.dto.UserResponse;
import aded.first_web_api.dto.UserUpdateRequest;
import aded.first_web_api.excepction.UserNotFoundException;
import aded.first_web_api.model.User;
import aded.first_web_api.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository userRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository userRepository, WalletService walletService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponse(user);
    }

    public void deleteUserById(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest req) {
        String hashed = passwordEncoder.encode(req.password());

        User user = new User(req.name(), req.email(), hashed);
        userRepository.save(user);

        walletService.createWalletWithSignupBonus(user.getId().longValue());

        return toResponse(user);
    }

    public UserResponse updateUser(Integer id, UserUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setName(req.name());
        user.setEmail(req.email());

        if (req.password() != null && !req.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
        }

        userRepository.save(user);
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
