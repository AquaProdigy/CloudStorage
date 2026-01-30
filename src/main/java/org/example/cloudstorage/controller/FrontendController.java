package org.example.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    @RequestMapping(value = {"/registration", "/login", "/files/**"})
    public String index() {
        return "forward:/index.html";
    }
}
