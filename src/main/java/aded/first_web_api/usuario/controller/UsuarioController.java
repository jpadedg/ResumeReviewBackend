package aded.first_web_api.usuario.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import aded.first_web_api.usuario.dto.UserCreateRequest;
import aded.first_web_api.usuario.dto.UserResponse;
import aded.first_web_api.usuario.dto.UserUpdateRequest;
import aded.first_web_api.usuario.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(usuarioService.getUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {
        usuarioService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest req) {
        UserResponse created = usuarioService.createUser(req);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequest req
    ) {
        return ResponseEntity.ok(usuarioService.updateUser(id, req));
    }
}
