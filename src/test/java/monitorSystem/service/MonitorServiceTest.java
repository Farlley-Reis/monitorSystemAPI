package monitorSystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MonitorServiceTest {

    private MonitorService monitorService;

    @BeforeEach
    void setUp() {
        monitorService = new MonitorService();
    }

    @Test
    void testSetURL_shouldCleanURL() {
        monitorService.setURL("\"https://www.instagram.com/\"");
        assertEquals("https://www.instagram.com/", monitorService.serverURL);
    }

    @Test
    void testEnsureValidURL_addsHttpsIfMissing() {
        monitorService.setURL("example.com");
        monitorService.ensureValidURL();
        assertTrue(monitorService.serverURL.startsWith("https://"));
    }

    @Test
    void testExtractSiteName_withWWW() {
        String site = monitorService.extractSiteName("https://www.google.com");
        assertEquals("google.com", site);
    }

    @Test
    void testExtractSiteName_withoutWWW() {
        String site = monitorService.extractSiteName("https://openai.com");
        assertEquals("openai.com", site);
    }

    @Test
    void testStart_shouldSetRunningTrue() {
        monitorService.start();
        assertTrue(monitorService.running);
    }

    @Test
    void testGetStatus_initialStatus() {
        assertEquals("Aguardando ação...", monitorService.getStatus());
    }

}
