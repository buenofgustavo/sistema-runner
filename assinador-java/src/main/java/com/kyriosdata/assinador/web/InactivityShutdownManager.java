package com.kyriosdata.assinador.web;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class InactivityShutdownManager {

    private long lastActivityTime;
    private long shutdownTimeoutMs;
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        this.lastActivityTime = System.currentTimeMillis();
        
        // Obter tempo limite a partir da propriedade do sistema, padrão é 30 minutos
        String timeoutProp = System.getProperty("assinador.shutdown-after", "30");
        long timeoutMinutes = 30;
        try {
            timeoutMinutes = Long.parseLong(timeoutProp);
        } catch (NumberFormatException e) {
            // Ignorar, usar padrão
        }
        
        this.shutdownTimeoutMs = timeoutMinutes * 60 * 1000;
        
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "inactivity-shutdown-thread");
            t.setDaemon(true);
            return t;
        });
        
        // Verificar a cada 10 segundos
        this.scheduler.scheduleAtFixedRate(this::checkInactivity, 10, 10, TimeUnit.SECONDS);
    }

    public synchronized void resetTimer() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    private synchronized void checkInactivity() {
        long inactiveTime = System.currentTimeMillis() - lastActivityTime;
        if (inactiveTime >= shutdownTimeoutMs) {
            shutdown();
        }
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        // Encerrar a aplicação JVM de forma limpa em outra thread
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
            System.exit(0);
        }).start();
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
