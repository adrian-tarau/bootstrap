package net.microfalx.bootstrap.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

@Controller
@RequestMapping("/error")
public class ErrorController extends BasicErrorController implements AnonymousController {

    public ErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @Override
    protected ModelAndView resolveErrorView(HttpServletRequest request, HttpServletResponse response, HttpStatus status, Map<String, Object> model) {
        model = new HashMap<>(model);
        updateModel(status, model);
        return super.resolveErrorView(request, response, status, unmodifiableMap(model));
    }

    private void updateModel(HttpStatus status, Map<String, Object> model) {
        String statusGlyph;
        status = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        statusGlyph = switch (status) {
            case BAD_REQUEST -> "fa-solid fa-circle-exclamation text-warning";
            case NOT_FOUND -> "fa-solid fa-file-circle-xmark text-secondary";
            default -> "fa-solid fa-server text-danger";
        };
        model.put("statusGlyph", statusGlyph);
    }
}
