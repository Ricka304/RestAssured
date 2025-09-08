package config;

import org.mockserver.integration.ClientAndServer;

public class MockServerConfig {
    public static ClientAndServer digilockerServiceMock;
    public static   ClientAndServer paymentServiceMock;
    public static ClientAndServer jioSignServiceMock;

    public static void startMockServers() {
        digilockerServiceMock = ClientAndServer.startClientAndServer(8080);
        paymentServiceMock = ClientAndServer.startClientAndServer(8081);
        jioSignServiceMock = ClientAndServer.startClientAndServer(8082);
    }

    public static void stopMockServers() {
        digilockerServiceMock.stop();
        paymentServiceMock.stop();
        jioSignServiceMock.stop();
    }
}