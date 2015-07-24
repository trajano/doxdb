package net.trajano.doxdb.rest;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.trajano.doxdb.Dox;

@WebServlet("hello")
public class Hello extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 3213175491599799032L;

    @EJB
    Dox dox;

    @Override
    protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException,
            IOException {

        resp.getWriter().print("hello " + dox);

    }
}
