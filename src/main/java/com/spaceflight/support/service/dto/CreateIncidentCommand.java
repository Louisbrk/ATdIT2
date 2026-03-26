package com.spaceflight.support.service.dto;

import com.spaceflight.support.domain.enums.IncidentSeverity;
import com.spaceflight.support.domain.enums.ResponsibleRole;

import java.util.UUID;

/**
 * Input data for creating a new incident.
 */
public record CreateIncidentCommand(
        UUID passengerId,
        String title,
        String description,
        IncidentSeverity severity,
        String reportedByName,
        ResponsibleRole reportedByRole
) {
}
