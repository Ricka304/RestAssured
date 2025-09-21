package config;


import context.TestContext;
import org.mockserver.integration.ClientAndServer;


import java.io.IOException;
import java.net.ServerSocket;


/**
 * Per-thread MockServer manager. Starts one ClientAndServer per test thread and
 * writes the mock base URLs into TestContext for test consumption.
 */
public final class MockServerConfig {


    private static final ThreadLocal<ClientAndServer> digilockerServer = new ThreadLocal<>();
    private static final ThreadLocal<ClientAndServer> paymentServer = new ThreadLocal<>();
    private static final ThreadLocal<ClientAndServer> jiosignServer = new ThreadLocal<>();


    private MockServerConfig() {}


    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Unable to find free port", e);
        }
    }


    public static void startMockServersForThread() {
// Digilocker
        int digPort = findFreePort();
        ClientAndServer dig = ClientAndServer.startClientAndServer(digPort);
        digilockerServer.set(dig);
        String digBase = "http://localhost:" + dig.getLocalPort();
        TestContext.get().setDigilockerBaseUrl(digBase);


// Payment
        int payPort = findFreePort();
        ClientAndServer pay = ClientAndServer.startClientAndServer(payPort);
        paymentServer.set(pay);
        String payBase = "http://localhost:" + pay.getLocalPort();
        TestContext.get().setPaymentBaseUrl(payBase);


// JioSign
        int jioPort = findFreePort();
        ClientAndServer jio = ClientAndServer.startClientAndServer(jioPort);
        jiosignServer.set(jio);
        String jioBase = "http://localhost:" + jio.getLocalPort();
        TestContext.get().setJioSignBaseUrl(jioBase);
    }


    public static ClientAndServer getDigilockerServerForThread() { return digilockerServer.get(); }
    public static ClientAndServer getPaymentServerForThread() { return paymentServer.get(); }
    public static ClientAndServer getJioSignServerForThread() { return jiosignServer.get(); }


    public static void stopMockServersForThread() {
        ClientAndServer dig = digilockerServer.get();
        if (dig != null) dig.stop();


        ClientAndServer pay = paymentServer.get();
        if (pay != null) pay.stop();


        ClientAndServer jio = jiosignServer.get();
        if (jio != null) jio.stop();


// cleanup threadlocals
        digilockerServer.remove();
        paymentServer.remove();
        jiosignServer.remove();


// clear test context mock URLs
        TestContext.get().setDigilockerBaseUrl(null);
        TestContext.get().setPaymentBaseUrl(null);
        TestContext.get().setJioSignBaseUrl(null);
    }
}