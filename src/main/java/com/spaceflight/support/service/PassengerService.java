package com.spaceflight.support.service;

import com.spaceflight.support.domain.model.Passenger;

import java.util.List;
import java.util.UUID;

public interface PassengerService {

    List<Passenger> getAllPassengers();

    Passenger getPassengerById(UUID passengerId);
}
