package com.spaceflight.support.repository.inmemory;

import com.spaceflight.support.domain.model.Incident;
import com.spaceflight.support.repository.IncidentRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryIncidentRepository implements IncidentRepository {

    private final Map<UUID, Incident> incidents = new LinkedHashMap<>();

    @Override
    public Incident save(Incident incident) {
        incidents.put(incident.getId(), incident);
        return incident;
    }

    @Override
    public Optional<Incident> findById(UUID id) {
        return Optional.ofNullable(incidents.get(id));
    }

    @Override
    public List<Incident> findAll() {
        return new ArrayList<>(incidents.values());
    }
}
