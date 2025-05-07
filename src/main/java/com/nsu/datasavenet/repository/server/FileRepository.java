package com.nsu.datasavenet.repository.server;

import com.nsu.datasavenet.model.server.FileEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByLoginAndAbsolutePathAndFileHashAndVersionAndFileRequestKey(
            String login,
            String absolutePath,
            String fileHash,
            int version,
            String fileRequestKey
    );
}
