package com.spaceflight.support.repository.inmemory;

import com.spaceflight.support.domain.model.Passenger;
import com.spaceflight.support.repository.PassengerRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryPassengerRepository implements PassengerRepository {

    private final Map<UUID, Passenger> passengers = new LinkedHashMap<>();

    @Override
    public Passenger save(Passenger passenger) {
        passengers.put(passenger.getId(), passenger);
        return passenger;
    }

    @Override
    public Optional<Passenger> findById(UUID id) {
        return Optional.ofNullable(passengers.get(id));
    }

    @Override
    public List<Passenger> findAll() {
        return new ArrayList<>(passengers.values());
    }
}
