package com.nsu.datasavenet.repository.server;

import com.nsu.datasavenet.model.server.EncryptMetadataEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerEncryptMetadataRepository extends JpaRepository<EncryptMetadataEntity, Long> {

    List<EncryptMetadataEntity> findAllByLogin(String login);
}
