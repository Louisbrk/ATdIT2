package com.spaceflight.support.repository;

import com.spaceflight.support.domain.model.Incident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository {

    Incident save(Incident incident);

    Optional<Incident> findById(UUID id);

    List<Incident> findAll();
}
