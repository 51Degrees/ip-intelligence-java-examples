/* *********************************************************************
 * This Original Work is copyright of 51 Degrees Mobile Experts Limited.
 * Copyright 2025 51 Degrees Mobile Experts Limited, Davidson House,
 * Forbury Square, Reading, Berkshire, United Kingdom RG1 3EU.
 *
 * This Original Work is licensed under the European Union Public Licence
 * (EUPL) v.1.2 and is subject to its terms as set out below.
 *
 * If a copy of the EUPL was not distributed with this file, You can obtain
 * one at https://opensource.org/licenses/EUPL-1.2.
 *
 * The 'Compatible Licences' set out in the Appendix to the EUPL (as may be
 * amended by the European Commission) shall be deemed incompatible for
 * the purposes of the Work and the provisions of the compatibility
 * clause in Article 5 of the EUPL shall not apply.
 *
 * If using the Work as, or as part of, a network application, by
 * including the attribution notice(s) required under Article 5 of the EUPL
 * in the end user terms of the application under an appropriate heading,
 * such notice(s) shall fulfill the requirements of that article.
 * ********************************************************************* */

package fiftyone.ipintelligence.examples.web;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.Servlet;
import java.util.Map;
import java.util.Scanner;

public class EmbedJetty {
    public static void runWebApp(String resourceBase, int port) throws Exception {
        Server server = startWebApp(resourceBase, port);
        System.out.format("Browse to http://localhost:%d using a 'private' window in your browser\n" +
                "Hit enter to stop server:", port);
        new Scanner(System.in).nextLine();
        server.stop();
    }

    public static Server startWebApp(String resourceBase, int port) throws Exception {
        Server server = new Server(port);
        Connector connector = new ServerConnector(server);
        server.addConnector(connector);

        // Create a WebAppContext.
        WebAppContext context = new WebAppContext();
        // Configure the path of the packaged web application (file or directory).
        context.setResourceBase(resourceBase);
        context.setDescriptor("WEB-INF/web.xml");

        // Link the context to the server.
        server.setHandler(context);

        server.start();
        return server;
    }

    public static void runServlet(String contextPath, int port, Class<? extends Servlet> servlet,
                                  Map<String, String> initParams) throws Exception {
        Server server = startServlet(contextPath, port, servlet, initParams);
        System.out.format("Browse to http://localhost:%d using a 'private' window in your browser\n" +
                "Hit enter to stop server:", port);
        new Scanner(System.in).nextLine();
        server.stop();
    }

    public static Server startServlet(String contextPath, int port,
                                      Class<? extends Servlet> servlet,
                                      Map<String, String> initParams) throws Exception {
        Server server = new Server(port);
        Connector connector = new ServerConnector(server);
        server.addConnector(connector);

        // Create a ServletContextHandler with contextPath.
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setSessionHandler(new SessionHandler());
        context.setContextPath("/");

        // Add the Servlet to the context.
        ServletHolder servletHolder = context.addServlet(servlet, contextPath);
        // Configure the Servlet with init-parameters.
        servletHolder.setInitParameters(initParams);

        // Link the context to the server.
        server.setHandler(context);

        server.start();
        return server;
    }
}
