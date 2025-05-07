package com.nsu.datasavenet.service;

import com.nsu.datasavenet.config.AppConfig;
import com.nsu.datasavenet.dto.peer.RestoreMetadataResponse;
import com.nsu.datasavenet.dto.peer.RestoreMetadataRequest;
import com.nsu.datasavenet.dto.peer.SaveFileRequest.Metadata;
import com.nsu.datasavenet.model.client.MetadataEntity;
import com.nsu.datasavenet.utils.DtoToModelConverters;
import com.nsu.datasavenet.utils.EncryptDecryptUtils;
import com.nsu.datasavenet.repository.client.MetadataRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MulticastPeerDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(MulticastPeerDiscovery.class);

    private static final String MULTICAST_ADDRESS = "230.0.0.0";

    private static final int PORT = 4446;

    private static final int BROADCAST_INTERVAL_SEC = 5;

    private static int webServerPort = -1;

    private MulticastSocket socket;

    private InetAddress group;

    private final Set<String> discoveredPeers = Collections.synchronizedSet(new HashSet<>());

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private final RestTemplate restTemplate = new RestTemplate();

    private final MetadataRepository metadataRepository;

    private final  AppConfig appConfig;

    public MulticastPeerDiscovery(MetadataRepository metadataRepository, AppConfig appConfig) {
        this.metadataRepository = metadataRepository;
        this.appConfig = appConfig;
    }

    @PostConstruct
    public void start() throws IOException {
        socket = new MulticastSocket(PORT);
        group = InetAddress.getByName(MULTICAST_ADDRESS);
        socket.joinGroup(group);
        LOG.info("Присоединились к мультикаст группе {}:{}", MULTICAST_ADDRESS, PORT);

        scheduler.scheduleAtFixedRate(this::broadcastPresence, 0, BROADCAST_INTERVAL_SEC, TimeUnit.SECONDS);
        scheduler.execute(this::listenForPeers);
    }

    @PreDestroy
    public void stop() throws IOException {
        socket.leaveGroup(group);
        socket.close();
        scheduler.shutdownNow();
        LOG.info("Остановлен мультикаст-обмен пирами");
    }

    private void broadcastPresence() {
        try {
            if (webServerPort == -1) {
                LOG.warn("Web server port ещё не инициализировался");
                return;
            }

            String msg = InetAddress.getLocalHost().getHostAddress()
                    + ":"
                    + webServerPort;
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), group, PORT);
            socket.send(packet);
            LOG.debug("Отправлен мультикаст по адресу {}:{} с web сервером: {}", MULTICAST_ADDRESS, PORT, msg);
        } catch (IOException e) {
            LOG.error("Ошибка при отправке мультикаста", e);
        }
    }

    private void listenForPeers() {
        byte[] buf = new byte[256];
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());

                String[] msg = received.split(":", 0);
                String localIp = InetAddress.getLocalHost().getHostAddress();

                //TODO Добавить !msg[0].equals(localIp) && при тестировании на нескольких машинах
                if (!msg[0].equals(localIp) && isPeerAlive(received)) {
                    if (!discoveredPeers.contains(received)) {
                        tryRestoreMetadataFromPeer(received);
                    }

                    discoveredPeers.add(received);
                    LOG.info("Обнаружен и подтверждён пир: {}", received);
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    LOG.error("Ошибка при приёме мультикаста", e);
                }
            }
        }
    }

    private void tryRestoreMetadataFromPeer(String ipAndPort) {
        RestoreMetadataResponse metadata = restTemplate.postForObject(
                "http://" + ipAndPort + "/internal/metadata/restore",new RestoreMetadataRequest(appConfig.login()), RestoreMetadataResponse.class);

        List<MetadataEntity> restoredMetadata =  metadata.restoredMetadata().stream().map( it -> EncryptDecryptUtils.decryptObjectWithPassword(it, appConfig.password(), Metadata.class)).map(
                DtoToModelConverters::convert).toList();

        for (MetadataEntity metadataEntity : restoredMetadata) {
            if (!metadataRepository.contains(metadataEntity)) {
                metadataRepository.save(metadataEntity);
                LOG.info("Metadata restored: {}", metadataEntity);
            } else {
                LOG.warn("Metadata already exists: {}", metadataEntity);
            }
        }
    }

    private boolean isPeerAlive(String ipAndPort) {
        try {
            String response = restTemplate.getForObject("http://" + ipAndPort + "/internal/ping", String.class);
            return "pong".equalsIgnoreCase(response);
        } catch (Exception e) {
            LOG.warn("Пир {} не ответил на ping", ipAndPort);
            return false;
        }
    }

    public Set<String> getDiscoveredPeers() {
        return new HashSet<>(discoveredPeers);
    }


    @Component
    public static class ServerInitializationListener implements ApplicationListener<ServletWebServerInitializedEvent> {

        @Override
        public void onApplicationEvent(ServletWebServerInitializedEvent event) {
            webServerPort = event.getWebServer().getPort();
            LOG.info("Web server started at port: {}", webServerPort);
        }
    }
}
