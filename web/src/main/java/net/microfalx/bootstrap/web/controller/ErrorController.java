package net.microfalx.bootstrap.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller("error")
public final class ErrorController {

    @GetMapping()
    public ModelAndView page() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("error/500");
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(HttpServletRequest request, Exception exception) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("exception", exception.getLocalizedMessage());
        mv.addObject("url", request.getRequestURL());
        mv.setViewName("error/500");
        return mv;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleNotFoundException(HttpServletRequest request, Exception exception) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("exception", exception.getLocalizedMessage());
        mv.addObject("url", request.getRequestURL());
        mv.setViewName("error/404");
        return mv;
    }
}
