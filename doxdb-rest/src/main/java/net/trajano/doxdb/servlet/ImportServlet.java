package net.trajano.doxdb.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/import")
@MultipartConfig()
public class ImportServlet extends HttpServlet {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 7055873387528138394L;

    @Override
    protected void doPost(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException,
            IOException {

        long c = 0;
        final InputStream cis = req.getPart("file").getInputStream();
        while (cis.read() != -1) {
            ++c;
            if (c % 1024024 == 0) {
                resp.getWriter().print(". ");
                resp.getWriter().flush();
            }
        }
        cis.close();
        resp.getWriter().print(c);
    }
}
