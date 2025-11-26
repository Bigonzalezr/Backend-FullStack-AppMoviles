package com.appmovil.msvc.admin.services;

import com.appmovil.msvc.admin.exception.AdminException;
import com.appmovil.msvc.admin.models.entities.Admin;
import com.appmovil.msvc.admin.repositories.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServicelmpl implements AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Admin findById(Long id) {
        return adminRepository.findById(id).orElseThrow(
            () -> new AdminException("El Admin con Id " + id + " no se encuentra en la base de datos")
        );
    }

    @Override
    @Transactional
    public Admin save(Admin admin) {
        // Validar que el username no exista
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new AdminException("El nombre de usuario ya está en uso");
        }
        // Validar que el email no exista
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new AdminException("El email ya está en uso");
        }
        return adminRepository.save(admin);
    }

    @Override
    @Transactional
    public Admin updateById(Long id, Admin admin) {
        return adminRepository.findById(id).map(existingAdmin -> {
            existingAdmin.setUsername(admin.getUsername());
            existingAdmin.setEmail(admin.getEmail());
            existingAdmin.setNombre(admin.getNombre());
            existingAdmin.setApellido(admin.getApellido());
            existingAdmin.setTelefono(admin.getTelefono());
            existingAdmin.setActivo(admin.getActivo());
            existingAdmin.setRol(admin.getRol());
            
            // Solo actualizar password si se proporciona uno nuevo
            if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
                existingAdmin.setPassword(admin.getPassword());
            }
            
            return adminRepository.save(existingAdmin);
        }).orElseThrow(
            () -> new AdminException("Admin con id " + id + " no encontrado")
        );
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new AdminException("Admin con id " + id + " no encontrado");
        }
        adminRepository.deleteById(id);
    }
}
