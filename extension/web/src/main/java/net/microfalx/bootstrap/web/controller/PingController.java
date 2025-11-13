package net.microfalx.bootstrap.web.controller;

import jakarta.annotation.security.PermitAll;
import net.microfalx.bootstrap.web.util.JsonResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/ping")
@Controller("ApplicationPingController")
@PermitAll
public class PingController {

    @GetMapping("")
    @ResponseBody()
    public JsonResponse<?> get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            return JsonResponse.success();
        } else {
            return JsonResponse.fail(JsonResponse.NOT_AUTHORIZED);
        }
    }

}
