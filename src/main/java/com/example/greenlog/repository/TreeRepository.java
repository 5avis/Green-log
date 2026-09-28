package com.example.greenlog.repository;

import com.example.greenlog.entity.Tree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TreeRepository extends JpaRepository<Tree, Long> {

    List<Tree> findByPlantationDriveId(Long plantationDriveId);

    List<Tree> findBySpeciesIgnoreCase(String species);

    long countByStatusIgnoreCase(String status);

    @Query("SELECT DISTINCT t.species FROM Tree t ORDER BY t.species ASC")
    List<String> findDistinctSpecies();

    /**
     * Trees due for check-in:
     * Only alive trees that have either never been checked in,
     * or haven't had a check-in on or after the specified cutoff date.
     */
    @Query("SELECT t FROM Tree t WHERE UPPER(t.status) <> 'DEAD' AND " +
           "(t.checkIns IS EMPTY OR NOT EXISTS (" +
           "  SELECT c FROM CheckIn c WHERE c.tree = t AND c.checkInDate >= :sinceDate" +
           ")) ORDER BY t.id ASC")
    List<Tree> findTreesDueForCheckIn(@Param("sinceDate") LocalDate sinceDate);
}
