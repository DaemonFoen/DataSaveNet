package com.nsu.datasavenet.controller;


import com.nsu.datasavenet.dto.peer.RestoreMetadataResponse;
import com.nsu.datasavenet.dto.peer.RestoreFileInternalRequest;
import com.nsu.datasavenet.dto.peer.RestoreMetadataRequest;
import com.nsu.datasavenet.dto.peer.SaveFileRequest;
import com.nsu.datasavenet.service.FileHolderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class P2PGateway {

    private final FileHolderService fileHolderService;

    public P2PGateway(FileHolderService fileHolderService) {
        this.fileHolderService = fileHolderService;
    }

    @GetMapping("/ping")
    public String pingPeer() {
        return "pong";
    }

    @PostMapping("/space/reserve")
    public boolean reserveSize(@RequestBody long fileSize) {
        return fileHolderService.reserveSize(fileSize);
    }

    @PostMapping("/space/release")
    public void releaseSize(@RequestBody long fileSize) {
        fileHolderService.releaseSize(fileSize);
    }

    @PostMapping("/save")
    public boolean saveFile(@RequestBody SaveFileRequest request) {
        return fileHolderService.saveFileRequest(request);
    }

    @PostMapping("/file/restore")
    public byte[] restoreFile(@RequestBody RestoreFileInternalRequest restoreFileInternalRequestRequest) {
        return fileHolderService.restoreFile(restoreFileInternalRequestRequest);
    }

    @PostMapping("/metadata/restore")
    public RestoreMetadataResponse restoreMetadata(@RequestBody RestoreMetadataRequest login) {
        return new RestoreMetadataResponse(fileHolderService.restoreMetadata(login));
    }
}
