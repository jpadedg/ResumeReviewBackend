package aded.first_web_api.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import aded.first_web_api.model.User;

@Repository
public interface UsuarioRepository extends JpaRepository<User, Integer> {
    User findByName(String name);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAll();
}
