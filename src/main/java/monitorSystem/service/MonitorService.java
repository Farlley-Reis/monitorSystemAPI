package monitorSystem.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

@Service
public class MonitorService {


    String serverURL = "";

    public boolean running = false;

    // Status atual do monitoramento sem ta fazendo nada
    private String status = "Aguardando ação...";


    // metodo que pega a url e trata ela com validacao

    public void setURL(String url) {
        String cleaned = url.trim().replaceAll("^\"|\"$", "");
        this.serverURL = cleaned;
        System.out.println("URL definida: " + this.serverURL);
    }

    // Liga o monitoramento
    public void start() {
        running = true;
    }

    // Pega o status atual do monitor (se tá online, offline, ou esperando)
    public String getStatus() {
        return status;
    }

    //
    @Scheduled(fixedRate = 5000)
    public void monitor() {
        if (!running) return;

        try {
            // Verifica se a URL tá certinha
            ensureValidURL();

            // Marca o tempo antes de fazer a requisição
            long start = System.currentTimeMillis();

            // Cria a conexão com a URL usando HTTP GET
            HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Pega o código de resposta HTTP da requisição
            int code = conn.getResponseCode();

            // Calcula o tempo que demorou a resposta
            long elapsed = System.currentTimeMillis() - start;

            // Pega só o nome do site da URL, tipo "google.com"
            String siteName = extractSiteName(serverURL);

            // Monta a mensagem
            String message = "Site: " + siteName + " - ";

            // Se o código for entre 200 e 299, tá online
            if (code >= 200 && code < 300) {
                status = "ONLINE";
                message += "ONLINE - HTTP " + code + " - Tempo: " + elapsed + "ms";
            } else {
                // Se não, tá offline
                status = "OFFLINE";
                message += "OFFLINE - HTTP " + code + " - Tempo: " + elapsed + "ms";
            }

            // Salva essa mensagem num arquivo de log e imprime no console
            log(message);

        } catch (UnknownHostException e) {
            status = "OFFLINE";
            String msg = "ERRO: Host desconhecido - não foi possível resolver o domínio (" + e.getMessage() + ")";
            log(msg);

        } catch ( IOException e) {
            status = "";
            String msg = "ERRO:  Insira um Link  (" + e.getMessage() + ")";
            log(msg);


        } catch (Exception e) {
            // Se der algum erro na requisição, marca offline e loga o erro
            status = "OFFLINE";
            log("ERRO: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    // Apaga o arquivo de logs, se existir
    public void clearLogs() {
        File logFile = new File("logs/monitor.log");
        try {
            if (logFile.exists()) {
                new FileWriter(logFile, false).close();
                System.out.println("Arquivo de logs apagado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método pra salvar as mensagens no arquivo e imprimir no console
    private void log(String msg) {
        try {
            // Cria a pasta "logs" se não existir
            File dir = new File("logs");
            if (!dir.exists()) dir.mkdirs();

            // Formata a mensagem com data/hora na frente
            String logLine = "[" + java.time.LocalDateTime.now() + "] " + msg + "\n";

            // Imprime no console
            System.out.print(logLine);

            // Escreve no arquivo monitor.log, adicionando no final do arquivo
            try (FileWriter writer = new FileWriter("logs/monitor.log", true)) {
                writer.write(logLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // colocar http:// se n tiver
    public void ensureValidURL() {
        if (!serverURL.startsWith("http://") && !serverURL.startsWith("https://")) {
            serverURL = "https://" + serverURL.trim();
        }
    }

    // Pega só o nome do site, tipo tira o "www." se tiver
    public String extractSiteName(String url) {
        try {
            String host = new URL(url).getHost();
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return "URL Inválida";
        }
    }
}
