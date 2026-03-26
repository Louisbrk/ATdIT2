package com.spaceflight.support.repository;

import com.spaceflight.support.domain.model.Passenger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PassengerRepository {

    Passenger save(Passenger passenger);

    Optional<Passenger> findById(UUID id);

    List<Passenger> findAll();
}
