package com.pashchenko.securityistiodemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityIstioDemoController {

    @Operation(summary = "Тестовый метод не закрытый авторизацией.")
    @GetMapping("/public")
    public String publicEndpoint() {
        return "Открытый API метод!";
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Тестовый метод для пользователей с ролью ROLE_USER или ROLE_ADMIN.")
    @GetMapping("/private")
    public String privateEndpoint() {
        return "Закрытый API метод, требующий аутентификации под пользователем с ролью ROLE_USER, ROLE_ADMIN!";
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Тестовый метод для пользователей с ролью ROLE_ADMIN.")
    @GetMapping("/private-admin")
    public String privateAdminEndpoint() {
        return "Закрытый API метод, требующий аутентификации под пользователем с ролью ROLE_ADMIN!";
    }
}
