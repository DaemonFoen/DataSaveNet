package com.nsu.datasavenet.dto.peer;

import java.io.Serializable;

public record RestoreFileInternalRequest(String login, String absolutePath, String fileHash, int version, String fileRequestKey) implements Serializable{

    private static final long serialVersionUID = 1L;

}

