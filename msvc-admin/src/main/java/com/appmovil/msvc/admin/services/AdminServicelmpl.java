package com.appmovil.msvc.admin.services;

import com.appmovil.msvc.admin.exception.AdminException;
import com.appmovil.msvc.admin.models.entities.Admin;
import com.appmovil.msvc.admin.repositories.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AdminServicelmpl implements AdminService {
    @Autowired
    private AdminRepository adminRepository;
    @Override
    public List<Admin> findAll() {return this.adminRepository.findAll();}

    @Override
    public Admin findById(Long id) {
        return this.adminRepository.findById(id).orElseThrow(
                () -> new AdminException("El Admin con Id"+ id +" no se encuntra el la base de datos")
        );
    }

    @Override
    public Admin save(Admin admin) {
        return  this.adminRepository.save(admin);
    }

    @Override
    public Admin updateById(Long id, UpdateAdminDTO updateAdminDTO){
        return adminRepository.findById(id).map(admin -> {
            admin.setNombres(updateAdminDTO.getNombres());
            admin.setApellidos(updateAdminDTO.getApellidos());
            admin.setFechaNacimiento(updateAdminDTO.getFechaNacimiento());
            admin.setCorreo(updateAdminDTO.getCorreo());
            admin.setContraseña(updateAdminDTO.getContraseña());
            return adminRepository.save(admin);
        }).orElseThrow(
                ()-> new AdminException("admin con id "+id+" no encontrado")
        );
    }

    @Override
    public void delete(Long id) {
        adminRepository.deleteById(id);
    }

    @Override
    public Admin cambiarEstadoCuenta(Long id, EstadoAdminDTO estadoAdminDTO) {
        return adminRepository.findById(id).map(admin -> {
            admin.setCuentaActiva(estadoAdminDTO.getCuentaActiva());
            return adminRepository.save(admin);
        }).orElseThrow(
                ()-> new AdminException("Admin con id "+id+" no encontrado")
        );
    }
}
