package com.appmovil.msvc.admin.services;

import com.appmovil.msvc.admin.models.entities.admin;

import java.util.List;

public interface AdminService {
    List<Admin> findAll();
    Admin findById(Long id);
    Admin save(Admin admin);
    Admin updateById(Long id, UpdateAdminDTO updateAdminDTO);
    void delete(Long id);
    Admin cambiarEstadoCuenta(Long id, EstadoAdminDTO estadoAdminDTO );
}
