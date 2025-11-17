package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.model.Plan;
import com.crudzaso.crudcloud_backend.model.UsersPlans;
import com.crudzaso.crudcloud_backend.repository.UsersPlansRepository;
import com.crudzaso.crudcloud_backend.exception.NoActivePlanException;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class PlanUsageService {

    private final UsersPlansRepository usersPlansRepository;

    public PlanUsageService(UsersPlansRepository usersPlansRepository) {
        this.usersPlansRepository = usersPlansRepository;
    }

    public Plan resolveActivePlan(Long userId) {
        return usersPlansRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .stream()
                .map(UsersPlans::getPlan)
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getState()))
                .max(Comparator.comparingInt(Plan::getMaxInstances))
                .orElseThrow(() -> new NoActivePlanException("User has no active plan"));
    }

    public int getMaxInstances(Long userId) {
        return resolveActivePlan(userId).getMaxInstances();
    }

    public boolean allowCustomName(Long userId) {
        return true;
    }
}
