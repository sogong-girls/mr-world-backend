package dev.sogong_girls.mr_world.domain.tour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sogong_girls.mr_world.domain.tour.entity.AddonOption;

public interface AddonOptionRepository extends JpaRepository<AddonOption, Long> {

    public List<AddonOption> findAllByTourId(Long tourId);

}
