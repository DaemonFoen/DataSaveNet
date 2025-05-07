package com.nsu.datasavenet.dto.user;

public record RestoreFileRequest(String path,
                                 int version) {
}
