package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args) {
    final var server = new Server();

    // Обработчик для /test
    server.addHandler("GET", "/test", (request, responseStream) -> {
      String param1 = request.getQueryParam("param1");
      String param2 = request.getQueryParam("param2");

      String response = "Получены параметры: param1=" + param1 + ", param2=" + param2;
      byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

      responseStream.write((
              "HTTP/1.1 200 OK\r\n" +
                      "Content-Type: text/plain; charset=utf-8\r\n" +
                      "Content-Length: " + responseBytes.length + "\r\n" +
                      "Connection: close\r\n" +
                      "\r\n"
      ).getBytes());
      responseStream.write(responseBytes);
      responseStream.flush();
    });

    // Обработчик для classic.html
    server.addHandler("GET", "/classic.html", (request, responseStream) -> {
      String htmlContent = "<!doctype html>\n" +
              "<html lang=\"en\">\n" +
              "<head><meta charset=\"UTF-8\"></head>\n" +
              "<body>\n" +
              "<h1>Classic Demo</h1>\n" +
              "<p>Current time is: " + LocalDateTime.now() + "</p>\n" +
              "</body>\n" +
              "</html>";

      byte[] content = htmlContent.getBytes(StandardCharsets.UTF_8);
      responseStream.write((
              "HTTP/1.1 200 OK\r\n" +
                      "Content-Type: text/html; charset=utf-8\r\n" +
                      "Content-Length: " + content.length + "\r\n" +
                      "Connection: close\r\n" +
                      "\r\n"
      ).getBytes());
      responseStream.write(content);
      responseStream.flush();
    });

    server.listen(9999);
  }
}