package com.example.greenlog.repository;

import com.example.greenlog.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    List<Volunteer> findAllByOrderByTotalTreesPlantedDescIdAsc();
}
