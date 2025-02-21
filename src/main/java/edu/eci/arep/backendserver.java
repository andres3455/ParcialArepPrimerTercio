package edu.eci.arep;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class backendserver {

    private static final int PORT = 4000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 BackendServer ejecutándose en el puerto " + PORT + "...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String requestLine = in.readLine();
                    if (requestLine == null) continue;

                    System.out.println("[DEBUG] Solicitud recibida: " + requestLine);

                    if (requestLine.startsWith("GET /compreflex?comando=")) {
                        String query = requestLine.split(" ")[1];
                        String comando = URLDecoder.decode(query.split("=")[1], "UTF-8");
                        System.out.println("[DEBUG] Comando extraído: " + comando);

                        Object result = command(comando);
                        String jsonResponse = "{\"resultado\": \"" + result + "\"}";

                        sendResponse(out, "200 OK", jsonResponse, "application/json");
                    } else {
                        sendResponse(out, "404 Not Found", "{\"error\": \"Ruta no encontrada\"}", "application/json");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendResponse(PrintWriter out, String status, String body, String contentType) {
        out.println("HTTP/1.1 " + status);
        out.println("Access-Control-Allow-Origin: *"); // Permitir solicitudes desde el cliente
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + body.length());
        out.println();
        out.println(body);
        out.flush();
    }

    //metodo para manejar la logica del comando
    private static Object command(String comando) {
        try {
            String[] partes = comando.split("[(),]");
            String metodoTipo = partes[0].trim();
            String clase = partes[1].trim();
            String metodoNombre = partes[2].trim();

            List<Object> args = new ArrayList<>();
            List<Class<?>> paramTypes = new ArrayList<>();

            if (metodoTipo.equals("unaryInvoke")) {
                String tipo = partes[3].trim();
                String valor = partes[4].trim();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
