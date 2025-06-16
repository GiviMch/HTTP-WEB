package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
  public static void main(String[] args) {
    final var server = new Server();

    // Обработчик для GET /messages
    server.addHandler("GET", "/messages", (request, responseStream) -> {
      String response = "GET messages handler response";
      responseStream.write((
              "HTTP/1.1 200 OK\r\n" +
                      "Content-Type: text/plain\r\n" +
                      "Content-Length: " + response.length() + "\r\n" +
                      "Connection: close\r\n" +
                      "\r\n" +
                      response
      ).getBytes());
      responseStream.flush();
    });

    // Обработчик для POST /messages
    server.addHandler("POST", "/messages", (request, responseStream) -> {
      System.out.println("Received message: " + request.getBody());
      responseStream.write((
              "HTTP/1.1 200 OK\r\n" +
                      "Content-Type: text/plain\r\n" +
                      "Content-Length: 0\r\n" +
                      "Connection: close\r\n" +
                      "\r\n"
      ).getBytes());
      responseStream.flush();
    });

    // Обработчик для GET /classic.html (пример из предыдущей задачи)
    server.addHandler("GET", "/classic.html", (request, responseStream) -> {
      try {
        Path filePath = Path.of(".", "public", "classic.html");
        String template = Files.readString(filePath);
        byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();

        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        responseStream.write(content);
        responseStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    server.listen(9999);
  }
}