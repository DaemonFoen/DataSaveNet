package com.nsu.datasavenet.controller;

import com.nsu.datasavenet.config.AppConfig;
import com.nsu.datasavenet.dto.peer.SaveFileRequest.Metadata;
import com.nsu.datasavenet.dto.user.RestoreFileRequest;
import com.nsu.datasavenet.dto.user.RestoreFileResponse;
import com.nsu.datasavenet.dto.user.SaveFileUserRequest;
import com.nsu.datasavenet.service.FileHolderService;
import com.nsu.datasavenet.service.SaveFileService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cli")
public class UserAPI {

    private static final Logger LOG = LoggerFactory.getLogger(UserAPI.class);
    private final FileHolderService fileHolderService;

    private final SaveFileService saveFileService;

    private final AppConfig config;

    public UserAPI(FileHolderService fileHolderService, SaveFileService saveFileService, AppConfig config) {
        this.fileHolderService = fileHolderService;
        this.saveFileService = saveFileService;
        this.config = config;
    }

    @PostConstruct
    public void init() {
        System.out.println("CLI контроллер запущен. Используйте команды через curl или HTTP-клиент.");
    }

    @PostMapping("/save")
    public void saveFile(@RequestBody SaveFileUserRequest request) throws Exception {
        LOG.info("Save file request: {}", request);
        saveFileService.save(request);
    }

    @GetMapping("/memory")
    public String memoryUsage() {
        long usedMemory = fileHolderService.getFreeSpace().get() / (1024 * 1024);
        if (config.limitSpace() == -1) {
            return "Используемая память : " + usedMemory + " MB, всего доступно: не ограниченно";
        }
        return "Используемая память : " + usedMemory + " MB, всего доступно: " + config.limitSpace() / (1024 * 1024) + " MB";
    }


    @PostMapping("/restore")
    public RestoreFileResponse restoreFile(@RequestBody RestoreFileRequest request) {
        LOG.info("Restore file request: {}", request);
        return new RestoreFileResponse(saveFileService.restoreFile(request));
    }

    //TODO implement file deletion
    @DeleteMapping("/delete")
    public String deleteFile(@RequestBody String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @GetMapping("/list")
    public List<Metadata> listFiles() {
        return saveFileService.getAllMetadata();
    }

}
