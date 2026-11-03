package dev.sogong_girls.mr_world.domain.souvenir.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sogong_girls.mr_world.domain.souvenir.entity.Souvenir;

public interface SouvenirRepository extends JpaRepository<Souvenir, Long> {
}
