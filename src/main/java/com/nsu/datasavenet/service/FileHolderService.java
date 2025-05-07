package com.nsu.datasavenet.service;

import com.nsu.datasavenet.config.AppConfig;
import com.nsu.datasavenet.dto.peer.RestoreFileInternalRequest;
import com.nsu.datasavenet.dto.peer.RestoreMetadataRequest;
import com.nsu.datasavenet.dto.peer.SaveFileRequest;
import com.nsu.datasavenet.model.server.EncryptMetadataEntity;
import com.nsu.datasavenet.model.server.FileEntity;
import com.nsu.datasavenet.repository.server.FileRepository;
import com.nsu.datasavenet.repository.server.ServerEncryptMetadataRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileHolderService {

    private static final Logger LOG = LoggerFactory.getLogger(FileHolderService.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final AppConfig config;

    private final AtomicLong freeSpace = new AtomicLong(Long.MAX_VALUE);

    private final ServerEncryptMetadataRepository encryptMetadataRepository;

    private final FileRepository fileRepository;

    public FileHolderService(AppConfig config, ServerEncryptMetadataRepository encryptMetadataRepository,
            FileRepository fileRepository) {
        this.config = config;
        this.encryptMetadataRepository = encryptMetadataRepository;
        this.fileRepository = fileRepository;
    }

    public AtomicLong getFreeSpace() {
        return freeSpace;
    }

    public boolean reserveSize(long size) {
        synchronized (freeSpace) {
            long val = freeSpace.addAndGet(-size);
            if (val < 0) {
                releaseSize(size);
                return false;
            }
            return true;
        }
    }
    
    public void releaseSize(long size) {
        freeSpace.addAndGet(-size);
    }

    @Transactional
    public boolean saveFileRequest(SaveFileRequest request) {
        LOG.info("Save file request {}", request);

        encryptMetadataRepository.save(
                new EncryptMetadataEntity(request.login(), request.metadata()));
        fileRepository.save(
                new FileEntity(request.login(), request.path(), request.hash(),
                        request.version(), request.fileRequestKey(),
                        request.content()));
        return true;
    }

    public byte[] restoreFile(RestoreFileInternalRequest request) {
        LOG.info("Restore file from peer request {}", request);

        List<FileEntity> result = fileRepository.findByLoginAndAbsolutePathAndFileHashAndVersionAndFileRequestKey(request.login(),
                request.absolutePath(), request.fileHash(), request.version(),
                request.fileRequestKey());

        if (result.isEmpty()) {
            throw new IllegalStateException("Файл не существует");
        }

        if (result.size() > 1) {
            throw new IllegalStateException("Два одинаковых файла по заданным параметрам не должно существовать");
        }

        return result.getFirst().getFile();
    }

    public List<byte[]> restoreMetadata(RestoreMetadataRequest request) {
        LOG.info("Restore metadata request {}", request);

        return encryptMetadataRepository.findAllByLogin(request.login()).stream().map(EncryptMetadataEntity::getEncryptMetadata).toList();
    }

}
