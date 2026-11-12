package br.com.sistemas.chamados.controller.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

final class Formularios {

    private Formularios() { }

    static Map<String, String> erros(BindingResult result) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError e : result.getFieldErrors()) {
            erros.putIfAbsent(e.getField(), e.getDefaultMessage());
        }
        return erros;
    }
}
