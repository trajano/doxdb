package net.trajano.doxdb.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("hello")
public class Hello extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 3213175491599799032L;

    @Override
    protected void doGet(HttpServletRequest req,
        HttpServletResponse resp) throws ServletException, IOException {

        resp.getWriter().print("hello");
        
    }
}
