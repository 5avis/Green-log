package com.example.greenlog.service;

import com.example.greenlog.entity.Volunteer;
import com.example.greenlog.exception.ResourceNotFoundException;
import com.example.greenlog.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    @Autowired
    public VolunteerService(VolunteerRepository volunteerRepository) {
        this.volunteerRepository = volunteerRepository;
    }

    public Volunteer createVolunteer(Volunteer volunteer) {
        if (volunteer.getName() == null || volunteer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Volunteer name is required.");
        }
        volunteer.setName(volunteer.getName().trim());
        if (volunteer.getTotalTreesPlanted() == null || volunteer.getTotalTreesPlanted() < 0) {
            volunteer.setTotalTreesPlanted(0);
        }
        java.util.Optional<Volunteer> existing = volunteerRepository.findByNameIgnoreCase(volunteer.getName());
        if (existing.isPresent()) {
            Volunteer found = existing.get();
            if (volunteer.getTotalTreesPlanted() != null && volunteer.getTotalTreesPlanted() > 0) {
                int current = found.getTotalTreesPlanted() != null ? found.getTotalTreesPlanted() : 0;
                found.setTotalTreesPlanted(current + volunteer.getTotalTreesPlanted());
            }
            return volunteerRepository.save(found);
        }
        return volunteerRepository.save(volunteer);
    }

    @Transactional(readOnly = true)
    public List<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Volunteer getVolunteerById(Long id) {
        return volunteerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found with id: " + id));
    }

    public Volunteer updateVolunteer(Long id, Volunteer updated) {
        Volunteer existing = getVolunteerById(id);
        existing.setName(updated.getName());
        if (updated.getTotalTreesPlanted() != null) {
            existing.setTotalTreesPlanted(updated.getTotalTreesPlanted());
        }
        return volunteerRepository.save(existing);
    }

    public void deleteVolunteer(Long id) {
        Volunteer existing = getVolunteerById(id);
        volunteerRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<Volunteer> getLeaderboard() {
        return volunteerRepository.findAllByOrderByTotalTreesPlantedDescIdAsc();
    }

    public void incrementTreesPlanted(Long volunteerId, int count) {
        if (volunteerId != null) {
            volunteerRepository.findById(volunteerId).ifPresent(volunteer -> {
                int current = volunteer.getTotalTreesPlanted() != null ? volunteer.getTotalTreesPlanted() : 0;
                volunteer.setTotalTreesPlanted(current + count);
                volunteerRepository.save(volunteer);
            });
        }
    }
}
