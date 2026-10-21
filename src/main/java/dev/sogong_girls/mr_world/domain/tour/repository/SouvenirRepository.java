package dev.sogong_girls.mr_world.domain.tour.repository;

import dev.sogong_girls.mr_world.domain.tour.entity.Souvenir;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SouvenirRepository extends JpaRepository<Souvenir, Long> {
}
