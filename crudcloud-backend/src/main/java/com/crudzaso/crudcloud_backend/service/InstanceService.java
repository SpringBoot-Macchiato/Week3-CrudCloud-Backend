package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.dto.InstanceRequest;
import com.crudzaso.crudcloud_backend.dto.InstanceResponse;
import com.crudzaso.crudcloud_backend.model.Instance;
import com.crudzaso.crudcloud_backend.repository.InstanceRepository;
import com.crudzaso.crudcloud_backend.util.AesGcmEncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final DatabaseAdminService dbAdminService;
    private final AesGcmEncryptionUtil crypto;

    public InstanceService(InstanceRepository instanceRepository,
                           EngineOrchestratorService dbAdminService,
                           AesGcmEncryptionUtil crypto) {
        this.instanceRepository = instanceRepository;
        this.dbAdminService = dbAdminService;
        this.crypto = crypto;
    }

    @Transactional
    public InstanceResponse createInstance(Long userId, InstanceRequest req, int planMaxInstances, boolean allowCustomName) throws Exception {
        long runningCount = instanceRepository.countByUserIdAndState(userId, "RUNNING");
        if (runningCount >= planMaxInstances) {
            throw new IllegalStateException("Plan instance limit reached");
        }

        String dbName = (req.getDbName() == null || req.getDbName().isBlank() || !allowCustomName)
                ? "db_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
                : req.getDbName();

        String userDb = (req.getUserDb() == null || req.getUserDb().isBlank())
                ? "u_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10)
                : req.getUserDb();

        String plainPassword = generatePassword();

        Instance inst = Instance.builder()
                .userId(userId)
                .engineId(req.getEngineId())
                .dbName(dbName)
                .userDb(userDb)
                .passwordEncrypted(crypto.encrypt(plainPassword))
                .host("127.0.0.1")
                .port(3306)
                .state("CREATING")
                .passwordShown(false)
                .createdAt(Instant.now())
                .build();

        Instance saved = instanceRepository.save(inst);

        try {
            Long engineId = req.getEngineId();
            String engineName = getEngineName(engineId);

            dbAdminService.createDatabaseAndUser(engineName, dbName, userDb, plainPassword);

            saved.setState("RUNNING");
            saved.setPasswordShown(true);
            saved.setUpdatedAt(Instant.now());
            instanceRepository.save(saved);

            InstanceResponse resp = mapToResponse(saved);
            resp.setPassword(plainPassword);
            return resp;

        } catch (Exception ex) {
            saved.setState("DELETED");
            saved.setUpdatedAt(Instant.now());
            instanceRepository.save(saved);
            throw ex;
        }
    }

    public List<InstanceResponse> listInstancesForUser(Long userId) {
        return instanceRepository.findByUserIdAndStateNot(userId, "DELETED")
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public InstanceResponse getInstance(Long id, Long userId) {
        Instance inst = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found"));
        if (!inst.getUserId().equals(userId)) throw new SecurityException("Access denied");
        return mapToResponse(inst);
    }

    @Transactional
    public void suspendInstance(Long id, Long userId) throws Exception {
        Instance inst = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found"));
        if (!inst.getUserId().equals(userId)) throw new SecurityException("Access denied");
        if (!"RUNNING".equals(inst.getState())) throw new IllegalStateException("Instance not running");

        String engine = getEngineName(inst.getEngineId());
        dbAdminService.lockUser(engine, inst.getUserDb());

        inst.setState("SUSPENDED");
        inst.setUpdatedAt(Instant.now());
        instanceRepository.save(inst);
    }

    @Transactional
    public void resumeInstance(Long id, Long userId) throws Exception {
        Instance inst = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found"));
        if (!inst.getUserId().equals(userId)) throw new SecurityException("Access denied");
        if (!"SUSPENDED".equals(inst.getState())) throw new IllegalStateException("Instance not suspended");

        String engine = getEngineName(inst.getEngineId());
        dbAdminService.unlockUser(engine, inst.getUserDb());

        inst.setState("RUNNING");
        inst.setUpdatedAt(Instant.now());
        instanceRepository.save(inst);
    }

    @Transactional
    public String rotatePassword(Long id, Long userId) throws Exception {
        Instance inst = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found"));
        if (!inst.getUserId().equals(userId)) throw new SecurityException("Access denied");

        String newPass = generatePassword();
        String engine = getEngineName(inst.getEngineId());
        dbAdminService.rotateUserPassword(engine, inst.getUserDb(), newPass);

        inst.setPasswordEncrypted(crypto.encrypt(newPass));
        inst.setPasswordShown(true);
        inst.setUpdatedAt(Instant.now());
        instanceRepository.save(inst);

        return newPass;
    }

    @Transactional
    public void deleteInstance(Long id, Long userId) throws Exception {
        Instance inst = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found"));
        if (!inst.getUserId().equals(userId)) throw new SecurityException("Access denied");

        String engine = getEngineName(inst.getEngineId());
        dbAdminService.dropDatabaseAndUser(engine, inst.getDbName(), inst.getUserDb());

        inst.setState("DELETED");
        inst.setUpdatedAt(Instant.now());
        instanceRepository.save(inst);
    }

    private InstanceResponse mapToResponse(Instance inst) {
        InstanceResponse r = new InstanceResponse();
        r.setId(inst.getId());
        r.setUserId(inst.getUserId());
        r.setEngineId(inst.getEngineId());
        r.setDbName(inst.getDbName());
        r.setUserDb(inst.getUserDb());
        r.setHost(inst.getHost());
        r.setPort(inst.getPort());
        r.setState(inst.getState());
        r.setPasswordShown(inst.getPasswordShown());
        r.setCreatedAt(inst.getCreatedAt());
        r.setUpdatedAt(inst.getUpdatedAt());
        return r;
    }

    private String generatePassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "A1!";
    }

    private String getEngineName(Long engineId) {
        return switch (engineId.intValue()) {
            case 1 -> "MySQL";
            case 2 -> "PostgreSQL";
            case 3 -> "SQLServer";
            default -> throw new IllegalArgumentException("Unsupported engine: " + engineId);
        };
    }
}
