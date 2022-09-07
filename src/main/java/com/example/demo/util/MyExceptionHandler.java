package com.example.demo.util;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public void exceptionHandler(Exception e, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.write(e.getMessage());
        out.flush();
        out.close();
    }
}
