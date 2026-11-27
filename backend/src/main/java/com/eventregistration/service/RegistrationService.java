package com.eventregistration.service;

import java.util.List;

import com.eventregistration.dto.RegistrationResponse;

public interface RegistrationService {

    RegistrationResponse registerForEvent(Long eventId, Long userId);

    RegistrationResponse cancelRegistration(Long registrationId, Long userId, boolean admin);

    List<RegistrationResponse> getRegistrationsByUser(Long userId);

    List<RegistrationResponse> getRegistrationsByEvent(Long eventId);
}
