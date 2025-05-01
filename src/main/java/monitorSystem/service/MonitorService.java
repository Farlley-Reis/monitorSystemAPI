package monitorSystem.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class MonitorService {

    private String serverURL = "https://www.google.com";
    private boolean running = false;
    private String status = "Aguardando ação...";

    public void setURL(String url) {
        String cleaned = url.trim().replaceAll("^\"|\"$", "");
        this.serverURL = cleaned;
        System.out.println("URL definida: " + this.serverURL);
    }

    public void start() {
        running = true;
    }

    public String getStatus() {
        return status;
    }

    @Scheduled(fixedRate = 5000)
    public void monitor() {
        if (!running) return;

        try {
            ensureValidURL();
            long start = System.currentTimeMillis();

            HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int code = conn.getResponseCode();
            long elapsed = System.currentTimeMillis() - start;
            String siteName = extractSiteName(serverURL);
            String message = "Site: " + siteName + " - ";

            if (code >= 200 && code < 300) {
                status = "ONLINE";
                message += "ONLINE - HTTP " + code + " - Tempo: " + elapsed + "ms";
            } else {
                status = "OFFLINE";
                message += "OFFLINE - HTTP " + code + " - Tempo: " + elapsed + "ms";
            }

            log(message);
        } catch (Exception e) {
            status = "OFFLINE";
            log("ERRO: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    public void clearLogs() {
        File logFile = new File("logs/monitor.log");
        try {
            if (logFile.exists()) {
                // substitui o conteúdo por vazio
                new FileWriter(logFile, false).close();
                System.out.println("Arquivo de logs apagado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String msg) {
        try {
            File dir = new File("logs");
            if (!dir.exists()) dir.mkdirs();

            String logLine = "[" + java.time.LocalDateTime.now() + "] " + msg + "\n";
            System.out.print(logLine);
            try (FileWriter writer = new FileWriter("logs/monitor.log", true)) {
                writer.write(logLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureValidURL() {
        if (!serverURL.startsWith("http://") && !serverURL.startsWith("https://")) {
            serverURL = "https://" + serverURL.trim();
        }
    }

    private String extractSiteName(String url) {
        try {
            String host = new URL(url).getHost();
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return "URL Inválida";
        }
    }
}
