package com.nsu.datasavenet.utils;

import com.nsu.datasavenet.dto.peer.SaveFileRequest.Metadata;
import com.nsu.datasavenet.model.client.MetadataEntity;

public class DtoToModelConverters {

    public static MetadataEntity convert(Metadata metadata) {
        MetadataEntity entity = new MetadataEntity();
        entity.setAbsolutePath(metadata.path());
        entity.setFileHash(metadata.hash());
        entity.setVersion(metadata.version());
        entity.setPeerAddress(metadata.peerAddress());
        entity.setFileRequestKey(metadata.fileRequestKey());
        entity.setCreationDate(metadata.creationDate());
        return entity;
    }

    public static Metadata convert(MetadataEntity entity) {
        return new Metadata(entity.getVersion(), entity.getAbsolutePath(), entity.getFileHash(), entity.getFileRequestKey(), entity.getPeerAddress(), entity.getCreationDate());
    }

}
