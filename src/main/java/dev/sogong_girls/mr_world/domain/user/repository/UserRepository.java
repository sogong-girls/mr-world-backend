package dev.sogong_girls.mr_world.domain.user.repository;

import dev.sogong_girls.mr_world.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
