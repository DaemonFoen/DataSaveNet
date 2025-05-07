package com.nsu.datasavenet.dto.peer;

import java.io.Serializable;
import java.time.LocalDateTime;

public record SaveFileRequest(int version, String login, String path, String hash, byte[] content, String fileRequestKey,
                              byte[] metadata) implements Serializable {

    private static final long serialVersionUID = 1L;

    public record Metadata(int version, String path, String hash, String fileRequestKey, String peerAddress, LocalDateTime creationDate) implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
