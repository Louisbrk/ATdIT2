package com.spaceflight.support.service.impl;

import com.spaceflight.support.service.SupportAvailabilityService;

public class SupportAvailabilityServiceImpl implements SupportAvailabilityService {

    private boolean onboardSupportAvailable;

    public SupportAvailabilityServiceImpl(boolean onboardSupportAvailable) {
        this.onboardSupportAvailable = onboardSupportAvailable;
    }

    @Override
    public boolean isOnboardSupportAvailable() {
        return onboardSupportAvailable;
    }

    @Override
    public void setOnboardSupportAvailable(boolean available) {
        this.onboardSupportAvailable = available;
    }
}
