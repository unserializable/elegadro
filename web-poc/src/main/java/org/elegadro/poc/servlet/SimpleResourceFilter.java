package org.elegadro.poc.servlet;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * Bypass Aranea resource importer.
 *
 * @author Taimo Peelo
 */
@Slf4j
public class SimpleResourceFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURL = httpServletRequest.getRequestURL().toString();
        String servletPath = httpServletRequest.getServletPath();

        if (log.isDebugEnabled()) {
            log.debug("requestUrl ='" + requestURL + "'");
            log.debug("servletPath ='" + servletPath + "'");
        }

        if (servletPath.startsWith("/assets/img/")) {
            serveImg(request.getServletContext(), response, "/patternfly/img/" + servletPath.substring("/assets/img/".length()));
        } else if (requestURL.endsWith(".woff2")) {
            serveWoff2(request.getServletContext(), response, "/patternfly/fonts" + requestURL.substring(requestURL.lastIndexOf('/')));
        } else if (requestURL.endsWith(".woff")) {
            serveWoff(request.getServletContext(), response, "/patternfly/fonts" + requestURL.substring(requestURL.lastIndexOf('/')));
        } else if (requestURL.endsWith(".ttf")) {
            serveTtf(request.getServletContext(), response, "/patternfly/fonts" + requestURL.substring(requestURL.lastIndexOf('/')));
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    private void serveImg(ServletContext servletContext, ServletResponse response, String path) throws IOException {
        response.setContentType(heuristic(path));
        serveResource(servletContext, response, path);
    }

    private void serveWoff(ServletContext servletContext, ServletResponse response, String path) throws IOException {
        response.setContentType(heuristic(path));
        serveResource(servletContext, response, path);
    }

    private void serveWoff2(ServletContext servletContext, ServletResponse response, String path) throws IOException {
        response.setContentType(heuristic(path));
        serveResource(servletContext, response, path);
    }

    private void serveTtf(ServletContext servletContext, ServletResponse response, String path) throws IOException {
        response.setContentType(heuristic(path));
        serveResource(servletContext, response, path);
    }

    private static String heuristic(String path) {
        if (path.endsWith(".ttf"))
            return "application/font-sfnt";
        if (path.endsWith(".woff2"))
            return "application/font-woff2";
        if (path.endsWith(".woff"))
            return "application/font-woff";

        if (path.endsWith(".jpg"))
            return "image/jpeg";

        if (path.endsWith(".png"))
            return "image/png";

        if (path.endsWith(".svg"))
            return "image/svg+xml";

        if (path.endsWith(".gif"))
            return "image/gif";

        if (path.endsWith(".ico"))
            return "image/vnd.microsoft.icon";

        return null;
    }

    private void serveResource(ServletContext servletContext, ServletResponse response, String path) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("About to serve resource from '" + path + "'" + " of ServletContext.");
        }
        ServletContextResource scResource = new ServletContextResource(servletContext, path);
        if (log.isDebugEnabled()) {
            log.debug("About to serve resource from " + scResource.getURL());
        }
        @Cleanup InputStream is = scResource.getInputStream();
        IOUtils.copy(is, response.getOutputStream());
    }
}
