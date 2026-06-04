package com.kyriosdata.assinador.web;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class InactivityShutdownManagerTest {

    @Test
    void shouldResetTimerSuccessfully() throws Exception {
        InactivityShutdownManager manager = new InactivityShutdownManager();
        manager.init();

        Field lastActivityField = InactivityShutdownManager.class.getDeclaredField("lastActivityTime");
        lastActivityField.setAccessible(true);

        long initialTime = (long) lastActivityField.get(manager);
        
        // Esperar um milissegundo para garantir alteração no timestamp
        Thread.sleep(5);

        manager.resetTimer();
        long resetTime = (long) lastActivityField.get(manager);

        assertTrue(resetTime > initialTime, "O timer de inatividade deveria ser reiniciado com um novo timestamp");
        
        manager.destroy();
    }

    @Test
    void shouldConfigureTimeoutFromSystemProperty() throws Exception {
        System.setProperty("assinador.shutdown-after", "15");
        try {
            InactivityShutdownManager manager = new InactivityShutdownManager();
            manager.init();

            Field timeoutField = InactivityShutdownManager.class.getDeclaredField("shutdownTimeoutMs");
            timeoutField.setAccessible(true);
            long timeoutMs = (long) timeoutField.get(manager);

            assertEquals(15 * 60 * 1000L, timeoutMs, "O timeout configurado deveria ser de 15 minutos em milissegundos");
            
            manager.destroy();
        } finally {
            System.clearProperty("assinador.shutdown-after");
        }
    }
}
