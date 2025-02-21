package edu.eci.arep;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class facadeserver {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("FacadeServer is running on port 5000...");
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                     OutputStream outputStream = clientSocket.getOutputStream()) {

                    System.out.println("🔗 Connection received...");
                    String requestLine = in.readLine();
                    if (requestLine == null || requestLine.trim().isEmpty()) {
                        sendResponse(outputStream, "400 Bad Request", "Invalid Request", "text/plain");
                        continue;
                    }

                    String uriString = requestLine.split(" ")[1];
                    URI requestURI = new URI(uriString);
                    String path = requestURI.getPath();

                    if (path.equals("/cliente")) {
                        serveHtmlFile(outputStream, "cliente.html");
                    } else if (path.equals("/compreflex") && requestLine.startsWith("POST")) {
                        String body = readRequestBody(in);

                        if (body == null || body.isEmpty()) {
                            sendResponse(outputStream, "400 Bad Request", "{\"error\": \"Empty request body\"}", "application/json");
                            continue;
                        }

                        String outputLine = sendToBackend(body);

                        System.out.println("[DEBUG] Sending response to client: " + outputLine);

                        // 🔹 Asegurar que el servidor envía los encabezados CORS y la respuesta correctamente
                        sendResponse(outputStream, "200 OK", outputLine, "application/json");
                    }


                    else {
                        sendResponse(outputStream, "404 Not Found", "404 Not Found", "text/plain");
                    }

                } catch (IOException | URISyntaxException e) {
                    System.err.println("❌ Error processing request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Could not start server on port 5000: " + e.getMessage());
        }
    }

    private static void serveHtmlFile(OutputStream outputStream, String s) throws IOException {
        File file = new File("target/classes/cliente.html");
        if (!file.exists()) {
            sendResponse(outputStream, "404 Not Found", "<h1>404 - File Not Found</h1>", "text/html");
            return;
        }

        String htmlContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        sendResponse(outputStream, "200 OK", htmlContent, "text/html");
    }


    private static String sendToBackend(String body) {
        try (Socket backendSocket = new Socket("localhost", 4000);
             PrintWriter out = new PrintWriter(backendSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(backendSocket.getInputStream(), StandardCharsets.UTF_8))) {

            // Extraer manualmente el valor de "command"
            String command = extractCommand(body);
            System.out.println("[DEBUG] Extracted Command: '" + command + "'");

            // Enviar solo el comando limpio al backend
            out.println(command);

            // Leer la respuesta del backend
            String response = in.lines().collect(Collectors.joining());
            System.out.println("[DEBUG] Response from backend: " + response);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Backend communication failed\"}";
        }
    }

    // Método para extraer el comando del JSON manualmente
    private static String extractCommand(String body) {
        body = body.trim();  // Eliminar espacios extra
        if (body.startsWith("{") && body.endsWith("}")) {
            int start = body.indexOf("\"command\":\"") + 11;
            if (start == 10) return "{\"error\": \"Invalid JSON format\"}"; // No encontró "command"
            int end = body.indexOf("\"", start);
            return body.substring(start, end);
        }
        return "{\"error\": \"Invalid JSON format\"}";
    }



    private static void sendResponse(OutputStream outputStream, String status, String body, String contentType) throws IOException {
        String response = "HTTP/1.1 " + status + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +  // 🔹 Permite solicitudes desde cualquier origen
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" + body;

        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }



    private static String readRequestBody(BufferedReader in) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;
        boolean isBody = false;

        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                isBody = true;  // La línea vacía indica el fin de los headers, comienza el body
                continue;
            }
            if (isBody) {
                body.append(line);
            }
        }

        System.out.println("[DEBUG] Extracted Body: '" + body.toString() + "'");
        return body.toString();
    }

}
//