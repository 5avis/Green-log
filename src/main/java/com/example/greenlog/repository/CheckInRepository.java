package com.example.greenlog.repository;

import com.example.greenlog.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByTreeIdOrderByCheckInDateDesc(Long treeId);

    List<CheckIn> findAllByOrderByCheckInDateDescIdDesc();

    long countByStatusReportedIgnoreCase(String statusReported);
}
