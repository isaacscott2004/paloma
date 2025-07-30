package com.paloma.paloma.javaServer.controllers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class StaticResourceController {

    @GetMapping("/index.js")
    public ResponseEntity<byte[]> getIndexJs() throws IOException {
        ClassPathResource jsFile = new ClassPathResource("static/index.js");

        byte[] bytes = jsFile.getInputStream().readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/javascript")
                .body(bytes);
    }

    @GetMapping("/index.css")
    public ResponseEntity<byte[]> getIndexCss() throws IOException {
        ClassPathResource cssFile = new ClassPathResource("static/index.css");

        byte[] bytes = cssFile.getInputStream().readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/css")
                .body(bytes);
    }
}
