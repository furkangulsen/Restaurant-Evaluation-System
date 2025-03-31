package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Ana sayfa ve genel yönlendirmeler için controller sınıfı
 */
@Controller
public class HomeController {

    /**
     * Ana sayfayı gösterir
     * 
     * @return Ana sayfa şablonu
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    /**
     * Test sayfasını gösterir
     * 
     * @return Test sayfası şablonu
     */
    @GetMapping("/test")
    public String showTestPage() {
        return "test";
    }
} 