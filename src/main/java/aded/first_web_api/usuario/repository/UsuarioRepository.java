package aded.first_web_api.usuario.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import aded.first_web_api.usuario.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UsuarioRepository extends JpaRepository<User, Integer> {
    User findByName(String name);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAll();
}
