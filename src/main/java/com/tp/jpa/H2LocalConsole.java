package com.tp.jpa;

import org.h2.tools.Server;

import java.sql.SQLException;

public class H2LocalConsole implements AutoCloseable {
    private static final String WEB_PORT = "8082";

    private final Server webServer;

    private H2LocalConsole(Server webServer) {
        this.webServer = webServer;
    }

    public static H2LocalConsole iniciar() {
        try {
            Server webServer = Server.createWebServer("-web", "-webPort", WEB_PORT).start();
            return new H2LocalConsole(webServer);
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo iniciar la consola web de H2", exception);
        }
    }

    public String getUrl() {
        return "http://localhost:" + WEB_PORT;
    }

    @Override
    public void close() {
        webServer.stop();
    }
}
