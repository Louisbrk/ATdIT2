package com.spaceflight.support.service.impl;

import com.spaceflight.support.domain.exception.EntityNotFoundException;
import com.spaceflight.support.domain.model.Passenger;
import com.spaceflight.support.repository.PassengerRepository;
import com.spaceflight.support.service.PassengerService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerServiceImpl(PassengerRepository passengerRepository) {
        this.passengerRepository = Objects.requireNonNull(passengerRepository, "passengerRepository must not be null");
    }

    @Override
    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll().stream()
                .sorted(Comparator.comparing(Passenger::getFullName))
                .toList();
    }

    @Override
    public Passenger getPassengerById(UUID passengerId) {
        Objects.requireNonNull(passengerId, "passengerId must not be null");
        return passengerRepository.findById(passengerId)
                .orElseThrow(() -> new EntityNotFoundException("Passenger not found: " + passengerId));
    }
}
