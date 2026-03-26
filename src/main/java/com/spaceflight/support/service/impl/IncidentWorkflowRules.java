package com.spaceflight.support.service.impl;

import com.spaceflight.support.domain.enums.IncidentStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class IncidentWorkflowRules {

    private static final Map<IncidentStatus, Set<IncidentStatus>> ALLOWED_TRANSITIONS = buildTransitions();

    private IncidentWorkflowRules() {
    }

    public static boolean canTransition(IncidentStatus currentStatus, IncidentStatus targetStatus) {
        return ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(IncidentStatus.class))
                .contains(targetStatus);
    }

    private static Map<IncidentStatus, Set<IncidentStatus>> buildTransitions() {
        Map<IncidentStatus, Set<IncidentStatus>> transitions = new EnumMap<>(IncidentStatus.class);
        transitions.put(IncidentStatus.NEW, EnumSet.of(IncidentStatus.ASSESSING, IncidentStatus.ESCALATED));
        transitions.put(IncidentStatus.ASSESSING, EnumSet.of(
                IncidentStatus.MONITORING,
                IncidentStatus.ESCALATED,
                IncidentStatus.RESOLVED
        ));
        transitions.put(IncidentStatus.MONITORING, EnumSet.of(
                IncidentStatus.ASSESSING,
                IncidentStatus.ESCALATED,
                IncidentStatus.RESOLVED
        ));
        transitions.put(IncidentStatus.ESCALATED, EnumSet.of(
                IncidentStatus.MONITORING,
                IncidentStatus.RESOLVED
        ));
        transitions.put(IncidentStatus.RESOLVED, EnumSet.noneOf(IncidentStatus.class));
        return transitions;
    }
}
