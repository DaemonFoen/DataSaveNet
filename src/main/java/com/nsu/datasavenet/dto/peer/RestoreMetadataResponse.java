package com.nsu.datasavenet.dto.peer;


import java.util.List;

public record RestoreMetadataResponse(List<byte[]> restoredMetadata){ }
