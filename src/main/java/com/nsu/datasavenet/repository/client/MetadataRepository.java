package com.nsu.datasavenet.repository.client;

import com.nsu.datasavenet.model.client.MetadataEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetadataRepository extends JpaRepository<MetadataEntity, Long> {

    default boolean contains(MetadataEntity metadataEntity) {
        return findByPeerAddressAndAbsolutePathAndFileHashAndVersionAndFileRequestKey(metadataEntity.getPeerAddress(),
                metadataEntity.getAbsolutePath(), metadataEntity.getFileHash(), metadataEntity.getVersion(),
                metadataEntity.getFileRequestKey()).isPresent();
    }

    List<MetadataEntity> findByAbsolutePath(String absolutePath);

    List<MetadataEntity> findByAbsolutePathAndVersion(String absolutePath, int version);

    Optional<MetadataEntity> findByPeerAddressAndAbsolutePathAndFileHashAndVersionAndFileRequestKey(
            String peerAddress,
            String absolutePath,
            String fileHash,
            int version,
            String fileRequestKey
    );
}
