package com.appmovil.msvc.usuarios.config;

import com.appmovil.msvc.usuarios.models.entities.Usuario;
import com.appmovil.msvc.usuarios.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            log.info("Inicializando datos de usuarios...");
            
            // Usuario administrador
            Usuario admin = Usuario.builder()
                    .username("admin")
                    .email("admin@levelupgamer.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Administrador")
                    .apellido("Sistema")
                    .telefono("555-0000")
                    .direccion("Calle Principal #123")
                    .rol("ADMIN")
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
            
            // Usuario normal 1
            Usuario user1 = Usuario.builder()
                    .username("juan_perez")
                    .email("juan.perez@email.com")
                    .password(passwordEncoder.encode("password123"))
                    .nombre("Juan")
                    .apellido("Pérez")
                    .telefono("555-1234")
                    .direccion("Av. Libertador #456")
                    .rol("USER")
                    .activo(true)
                    .build();
            usuarioRepository.save(user1);
            
            // Usuario normal 2
            Usuario user2 = Usuario.builder()
                    .username("maria_garcia")
                    .email("maria.garcia@email.com")
                    .password(passwordEncoder.encode("password123"))
                    .nombre("María")
                    .apellido("García")
                    .telefono("555-5678")
                    .direccion("Calle Los Álamos #789")
                    .rol("USER")
                    .activo(true)
                    .build();
            usuarioRepository.save(user2);
            
            // Usuario moderador
            Usuario moderator = Usuario.builder()
                    .username("mod_carlos")
                    .email("carlos.mod@levelupgamer.com")
                    .password(passwordEncoder.encode("mod123"))
                    .nombre("Carlos")
                    .apellido("Moderador")
                    .telefono("555-9999")
                    .direccion("Plaza Central #321")
                    .rol("MODERATOR")
                    .activo(true)
                    .build();
            usuarioRepository.save(moderator);
            
            log.info("Datos de usuarios inicializados correctamente. Total usuarios: {}", usuarioRepository.count());
        } else {
            log.info("Ya existen usuarios en la base de datos. Total: {}", usuarioRepository.count());
        }
    }
}
