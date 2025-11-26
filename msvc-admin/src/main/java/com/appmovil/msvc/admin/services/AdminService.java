package com.appmovil.msvc.admin.services;

import com.appmovil.msvc.admin.models.entities.Admin;

import java.util.List;

public interface AdminService {
    List<Admin> findAll();
    Admin findById(Long id);
    Admin save(Admin admin);
    Admin updateById(Long id, Admin admin);
    void deleteById(Long id);
}
