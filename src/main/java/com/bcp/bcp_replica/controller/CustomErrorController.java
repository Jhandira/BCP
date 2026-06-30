package com.bcp.bcp_replica.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        int statusCode = 500;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }

        model.addAttribute("status", statusCode);

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("error", "Página no encontrada");
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            model.addAttribute("error", "Acceso no autorizado");
        } else {
            model.addAttribute("error", "Error interno del servidor");
        }

        return "error";
    }
}