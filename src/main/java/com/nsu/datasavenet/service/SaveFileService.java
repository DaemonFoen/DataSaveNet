package com.nsu.datasavenet.service;

import com.nsu.datasavenet.config.AppConfig;
import com.nsu.datasavenet.dto.peer.RestoreFileInternalRequest;
import com.nsu.datasavenet.dto.peer.SaveFileRequest;
import com.nsu.datasavenet.dto.peer.SaveFileRequest.Metadata;
import com.nsu.datasavenet.dto.user.RestoreFileRequest;
import com.nsu.datasavenet.dto.user.SaveFileUserRequest;
import com.nsu.datasavenet.model.client.MetadataEntity;
import com.nsu.datasavenet.repository.client.MetadataRepository;
import com.nsu.datasavenet.utils.DtoToModelConverters;
import com.nsu.datasavenet.utils.EncryptDecryptUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SaveFileService {

    private static final Logger LOG = LoggerFactory.getLogger(SaveFileService.class);

    private static final int REPLICA_FACTOR = 2;

    private final MetadataRepository metadataRepository;

    private final MulticastPeerDiscovery peerDiscovery;

    private final RestTemplate restTemplate = new RestTemplate();

    private final AppConfig appConfig;


    public SaveFileService(MetadataRepository metadataRepository, MulticastPeerDiscovery peerDiscovery,
            AppConfig appConfig) {
        this.metadataRepository = metadataRepository;
        this.peerDiscovery = peerDiscovery;
        this.appConfig = appConfig;
    }

    public List<Metadata> getAllMetadata() {
        return metadataRepository.findAll().stream().map(DtoToModelConverters::convert).toList();
    }

    //TODO Обдумать случай сохранения файла без изменений
    public void save(SaveFileUserRequest request) throws IOException {
        File file = new File(request.path());
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Файл не существует по указанному пути: " + request.path());
        }

        String fileHash = getHashFromFile(file);
        List<MetadataEntity> existingMetadata = metadataRepository.findByAbsolutePath(request.path());
        int newVersion = existingMetadata.stream()
                .mapToInt(MetadataEntity::getVersion)
                .max()
                .orElse(0) + 1;

        byte[] payload;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            payload = fileInputStream.readAllBytes();
        }
        byte[] encryptedPayload = EncryptDecryptUtils.encryptObjectWithPassword(payload, request.password());

        int replicaCounter = 0;

        for (String peer : peerDiscovery.getDiscoveredPeers()) {
            boolean success = restTemplate.postForObject("http://" + peer + "/internal/space/reserve", file.length(),
                    Boolean.class);

            if (!success) {
                continue;
            }

            String requestKey = UUID.randomUUID().toString();
            Metadata metadata = new Metadata(newVersion, request.path(), fileHash, requestKey, peer, LocalDateTime.now());
            byte[] encryptedMetadata = EncryptDecryptUtils.encryptObjectWithPassword(metadata, request.password());

            SaveFileRequest saveFileRequest = new SaveFileRequest(newVersion, request.login(), request.path(), fileHash,
                    encryptedPayload, requestKey, encryptedMetadata);

            //TODO Обработать ошибку
            restTemplate.postForObject("http://" + peer + "/internal/save", saveFileRequest, Boolean.class);

            try {
                metadataRepository.save(DtoToModelConverters.convert(metadata));
            } catch (Exception exception) {
                LOG.error("Ошибка при сохранении метаданных");
                restTemplate.postForObject("http://" + peer + "/internal/space/release", file.length(), Boolean.class);
                throw exception;
            }

            replicaCounter++;
            if (replicaCounter == REPLICA_FACTOR) {
                break;
            }
        }

        if (replicaCounter != REPLICA_FACTOR) {
            LOG.warn("Число доступных пиров меньше требуемого фактора репликации. Реплицировано {}, необходимо {}", replicaCounter, REPLICA_FACTOR);
        }
    }

    public byte[] restoreFile(RestoreFileRequest request) {
        for (var metadata : metadataRepository.findByAbsolutePathAndVersion(request.path(), request.version())) {
            try {
                RestoreFileInternalRequest internalRequest = new RestoreFileInternalRequest(appConfig.login(),request.path(), metadata.getFileHash(), request.version(), metadata.getFileRequestKey());
                byte[] encryptedData = restTemplate.postForObject("http://" + metadata.getPeerAddress() + "/internal/file/restore", internalRequest, byte[].class);
                LOG.info("Restore file from {} with metadata {}", metadata.getPeerAddress(), metadata);
                return EncryptDecryptUtils.decryptObjectWithPassword(encryptedData, appConfig.password(), byte[].class);
            } catch (Exception exception) {
                throw new RuntimeException("Что то произошло при восстановлении файла");
            }
        }
        LOG.error("Не удалось восстановить файл по запросу {}", request);
        throw new RuntimeException("Файл не найден");
    }

    private String getHashFromFile(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] bytesBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new RuntimeException(
                    "Could not generate hash from file", ex);
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}
