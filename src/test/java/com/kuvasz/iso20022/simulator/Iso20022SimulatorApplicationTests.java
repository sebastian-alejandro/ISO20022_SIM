package com.kuvasz.iso20022.simulator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de contexto básico de la aplicación
 */
@SpringBootTest
@ActiveProfiles("test")
class Iso20022SimulatorApplicationTests {

    @Test
    void contextLoads() {
        // Test que verifica que el contexto de Spring se carga correctamente
    }
}
