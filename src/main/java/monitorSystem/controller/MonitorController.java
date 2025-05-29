package monitorSystem.controller;

import monitorSystem.service.MonitorService;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping("/set-url")
    public String setUrl(@RequestBody String url) {
        monitorService.setURL(url);
        return "URL configurada: " + url;
    }

    @GetMapping("/start")
    public String startMonitoring() {
        monitorService.start();
        return "Monitoramento iniciado!";
    }

    @GetMapping("/stop")
    public String stopMonitoring() {
        monitorService.stop();
        return "Monitoramento parado!";
    }


    @GetMapping("/status")
    public String getStatus() {
        return monitorService.getStatus();
    }

    @GetMapping("/logs")
    public String getLogs() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(Paths.get("logs/monitor.log").toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            return "Erro ao ler os logs.";
        }
        return sb.toString();
    }

    @DeleteMapping("/clear-logs")
    public String clearLogs() {
        monitorService.clearLogs();
        return "Logs apagados com sucesso!";
    }
}
