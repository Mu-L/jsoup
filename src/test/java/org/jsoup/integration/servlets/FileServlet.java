package org.jsoup.integration.servlets;

import org.jsoup.integration.ParseTest;
import org.jsoup.integration.TestServer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileServlet extends BaseServlet {
    public static final String Url;
    public static final String TlsUrl;
    static {
        TestServer.ServletUrls urls = TestServer.map(FileServlet.class);
        Url = urls.url;
        TlsUrl = urls.tlsUrl;
    }
    public static final String ContentTypeParam = "contentType";
    public static final String HtmlType = "text/html";
    static final String XmlType = "text/xml";
    public static final String SuppressContentLength = "surpriseMe";

    @Override
    protected void doIt(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String contentType = req.getParameter(ContentTypeParam);
        if (contentType == null) {
            contentType = HtmlType;
            if (req.getPathInfo().contains(".xml")) contentType = XmlType;
        }
        String location = req.getPathInfo();

        File file = ParseTest.getFile(location);
        if (file.exists()) {
            res.setContentType(contentType);
            if (file.getName().endsWith("gz"))
                res.addHeader("Content-Encoding", "gzip");
            if (req.getParameter(SuppressContentLength) == null)
                res.setContentLength((int) file.length());
            res.setStatus(HttpServletResponse.SC_OK);

            ServletOutputStream out = res.getOutputStream();
            Files.copy(file.toPath(), out);
            out.flush();
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public static String urlTo(String path) {
        return Url + path;
    }

    public static String tlsUrlTo(String path) {
        return TlsUrl + path;
    }
}
