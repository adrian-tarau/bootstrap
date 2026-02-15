package net.microfalx.bootstrap.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticController {

    @GetMapping("/robots.txt")
    public ResponseEntity<String> get() {
        return ResponseEntity.ok()
                .header("X-Robots-Tag", "noindex, nofollow, noarchive")
                .body(BODY);
    }

    private static final String BODY = """
            User-agent: *
            Disallow: /
            """;

}
