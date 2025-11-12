package com.crudzaso.crudcloud_backend.repository;

import com.crudzaso.crudcloud_backend.model.Instance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstanceRepository extends JpaRepository<Instance, Long> {
    List<Instance> findByUserIdAndStateNot(Long userId, String excludedState);
    long countByUserIdAndState(Long userId, String state);
}
