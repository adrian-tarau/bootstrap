package net.microfalx.bootstrap.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller("/error")
//@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(HttpServletRequest request, Exception exception) {
        return redirectTo(request, exception, 500);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleNotFoundException(HttpServletRequest request, Exception exception) {
        return redirectTo(request, exception, 404);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleBadRequest(HttpServletRequest request, Exception exception) {
        return redirectTo(request, exception, 400);
    }

    private ModelAndView redirectTo(HttpServletRequest request, Exception exception, int code) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("exception", exception.getLocalizedMessage());
        mv.addObject("url", request.getRequestURL());
        mv.setViewName("error/" + code);
        return mv;
    }
}
