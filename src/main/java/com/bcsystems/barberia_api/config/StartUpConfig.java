package com.bcsystems.barberia_api.config;

import com.bcsystems.barberia_api.service.LicenciaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class StartUpConfig {

    @Bean
    CommandLineRunner run(LicenciaService licenciaService) {
        return args -> {
            String clienteId = ClienteIdUtil.obtenerClienteId();

            licenciaService.registrarSiNoExiste(clienteId);

            boolean valido = licenciaService.validarLicencia(clienteId);

            if(!valido){
                System.out.println("Licencia no valida. Cerrando app");
                System.exit(0);
            }
            System.out.println("Licencia valida");
        };
    }

    @Scheduled(fixedRate = 1000)
    public void verificarLicencia() {
        String clienteId = ClienteIdUtil.obtenerClienteId();
        LicenciaService licenciaService = new LicenciaService();
        boolean valido = licenciaService.validarLicencia(clienteId);

        if (!valido) {
            System.out.println("Licencia expirada. Cerrando app...");
            System.exit(0);
        }

        System.out.println("Licencia sigue siendo valida");
    }

}
