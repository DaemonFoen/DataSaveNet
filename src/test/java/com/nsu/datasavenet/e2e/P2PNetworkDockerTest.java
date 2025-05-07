package com.nsu.datasavenet.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nsu.datasavenet.dto.user.RestoreFileRequest;
import com.nsu.datasavenet.dto.user.RestoreFileResponse;
import com.nsu.datasavenet.dto.user.SaveFileUserRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class P2PNetworkDockerTest {
//    static DockerComposeContainer<?> environment =
//            new DockerComposeContainer<>(new File("docker-compose.yml")).withBuild(false)
//                    .withExposedService("peer1", 8080, Wait.forLogMessage(".*Обнаружен и подтверждён пир:.*\\n", 2))
//                    .withExposedService("peer2", 8080, Wait.forLogMessage(".*Обнаружен и подтверждён пир:.*\\n", 2))
//                    .withExposedService("peer3", 8080, Wait.forLogMessage(".*Обнаружен и подтверждён пир:.*\\n", 2))
//                    .withTailChildContainers(true);

    private static final Network network = Network.newNetwork();

    static GenericContainer<?> peer1;

    static GenericContainer<?> peer2;

    static GenericContainer<?> peer3;

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void startPeers() {
//        ImageFromDockerfile image = new ImageFromDockerfile("peer-built-from-source", false)
//                .withFileFromPath(".", new File(".").toPath());

        ExecutorService executor = Executors.newFixedThreadPool(3);

        peer1 = new GenericContainer<>("datasavenet-peer1")
                .withNetwork(network)
                .withNetworkAliases("peer1")
                .withExposedPorts(8080)
                .waitingFor(Wait.forLogMessage(".*Обнаружен и подтверждён пир:.*", 2));
        peer1.setPortBindings(List.of("8081:8080"));

        peer2 = new GenericContainer<>("datasavenet-peer1")
                .withNetwork(network)
                .withNetworkAliases("peer2")
                .withExposedPorts(8080)
                .waitingFor(Wait.forLogMessage(".*Обнаружен и подтверждён пир:.*", 2));
        peer2.setPortBindings(List.of("8082:8080"));

        peer3 = new GenericContainer<>("datasavenet-peer1")
                .withNetwork(network)
                .withNetworkAliases("peer3")
                .withExposedPorts(8080)
                .waitingFor(Wait.forLogMessage(".*Обнаружен и подтверждён пир:.*", 2));
        peer3.setPortBindings(List.of("8083:8080"));

        executor.submit(() -> peer2.start());
        executor.submit(() -> peer3.start());
        peer1.start();
    }

    @AfterEach
    void stopPeers() {
        peer1.stop();
        peer2.stop();
        peer3.stop();
    }

    @Test
    void SaveAndRestoreFileUsecase() {
        testPeersAreUp();

        testSaveFile();

        testFileReplicaFactor();

        testFileRestoration();
    }

    @Test
    void RestoreMetadataUsecase() {
        testPeersAreUp();

        testSaveFile();

        testMetadataRestoration();

        testFileRestoration();
    }

    void testPeersAreUp() {
        for (String port : new String[]{"8081", "8082", "8083"}) {

            ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/internal/ping",
                    String.class);

            assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode(),
                    "Peer on port " + port + " is not responding");
        }
    }

    void testSaveFile() {
        String host = peer1.getHost();
        var request = new SaveFileUserRequest(
                "login", "password", "/test_data.txt"
        );

        ResponseEntity<String> response = restTemplate.postForEntity("http://" + host + ":" + 8081 + "/cli/save",
                request, String.class);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    }

    void testFileReplicaFactor() {
        String peer2IP = peer2.getContainerInfo()
                .getNetworkSettings()
                .getNetworks()
                .values()
                .iterator()
                .next()
                .getIpAddress();
        String peer3IP = peer3.getCurrentContainerInfo()
                .getNetworkSettings()
                .getNetworks()
                .values()
                .iterator()
                .next()
                .getIpAddress();

        ResponseEntity<List> peer1Response = restTemplate.getForEntity("http://localhost:" + 8081 + "/cli/list",
                List.class);

        assertEquals(HttpStatusCode.valueOf(200), peer1Response.getStatusCode());
        assertEquals(2, peer1Response.getBody().size());
        assertEquals(true,
                peer1Response.getBody().stream()
                        .anyMatch(it -> ((LinkedHashMap) it).get("peerAddress").equals(peer2IP + ":8080")));
        assertEquals(true,
                peer1Response.getBody().stream()
                        .anyMatch(it -> ((LinkedHashMap) it).get("peerAddress").equals(peer3IP + ":8080")));
    }

    void testFileRestoration() {
        String host = peer1.getHost();
        var request = new RestoreFileRequest("/test_data.txt", 1);

        ResponseEntity<RestoreFileResponse> response = restTemplate.postForEntity(
                "http://" + host + ":" + 8081 + "/cli/restore",
                request, RestoreFileResponse.class);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertEquals("Some data to test\n", new String(response.getBody().file(), StandardCharsets.UTF_8));
    }

    void testMetadataRestoration() {
        peer1.stop();
        peer1.start();

        testFileReplicaFactor();
    }
}
