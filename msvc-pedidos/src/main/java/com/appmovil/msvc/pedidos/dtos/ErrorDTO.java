package com.appmovil.msvc.pedidos.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class ErrorDTO {

    private Integer status;
    private Date date;
    private Map<String, String> errors;

    public void setStatus(int status) {
    }

    public void setDate(Date date) {
    }

    public void setErrors(Map<String, String> errorMap) {
    }
}