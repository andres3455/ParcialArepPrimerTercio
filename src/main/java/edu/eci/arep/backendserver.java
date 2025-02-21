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
            System.out.println("BackendServer ejecutándose en el puerto " + PORT + "...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String requestLine = in.readLine();
                    if (requestLine == null) continue;

                    System.out.println(" Solicitud recibida: " + requestLine);

                    if (requestLine.startsWith("GET /compreflex?comando=")) {
                        String query = requestLine.split(" ")[1];
                        String comando = URLDecoder.decode(query.split("=")[1], "UTF-8");
                        System.out.println("Comando extraído: " + comando);

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
        out.println("Access-Control-Allow-Origin: *");
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + body.length());
        out.println();
        out.println(body);
        out.flush();
    }

    //metodo para manejar la logica del comando
    private static Object command(String comando) {
        try {
            String[] parts = comando.split("[(),]");
            String metodo = parts[0].trim(); // se ubica el tipo de metodo (binary o invoke)
            String clase = parts[1].trim(); // se ubica la clase (java.lang)
            String MethodName = parts[2].trim(); // se ubica el metodo de esaa clase (Math)

            List<Object> a = new ArrayList<>();
            List<Class<?>> bTypes = new ArrayList<>();

            if (metodo.equals("unaryInvoke")) {
                String tipo = parts[3].trim();
                String valor = parts[4].trim();

                Object arg = argument(tipo, valor);
                a.add(arg);
                bTypes.add(primitivetype(tipo));
            } else {
                return "Error: Comando no soportado";
            }

            Class<?> clazz = Class.forName(clase);
            Method method = clazz.getMethod(MethodName, bTypes.toArray(new Class[0]));
            return method.invoke(null, a.toArray());

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static Object argument(String Type, String value) {
        return switch (Type) {
            case "int" -> Integer.parseInt(value);
            case "double" -> Double.parseDouble(value);
            default -> throw new IllegalArgumentException("Tipo no soportado: " + Type);
        };
    }

    private static Class<?> primitivetype(String Type) {
        return switch (Type) {
            case "int" -> int.class;
            case "double" -> double.class;
            default -> throw new IllegalArgumentException("no esta en la lista de tipos primitivos" + Type);
        };
    }
}