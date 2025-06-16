package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService threadPool;
    private final List<String> validPaths;

    public Server(int port, List<String> validPaths) {
        this.port = port;
        this.validPaths = validPaths;
        this.threadPool = Executors.newFixedThreadPool(64);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.execute(() -> handleConnection(socket));
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            String requestLine = in.readLine();
            if (requestLine == null) return;

            String[] parts = requestLine.split(" ");
            if (parts.length != 3) return;

            String path = parts[1];
            if (!validPaths.contains(path)) {
                sendNotFoundResponse(out);
                return;
            }

            Path filePath = Path.of(".", "public", path);
            if (path.equals("/classic.html")) {
                handleClassicHtml(filePath, out);
            } else {
                handleStaticFile(filePath, out);
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private void handleStaticFile(Path filePath, BufferedOutputStream out) throws IOException {
        String mimeType = Files.probeContentType(filePath);
        long length = Files.size(filePath);

        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());

        Files.copy(filePath, out);
        out.flush();
    }

    private void handleClassicHtml(Path filePath, BufferedOutputStream out) throws IOException {
        String template = Files.readString(filePath);
        byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();

        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + Files.probeContentType(filePath) + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());

        out.write(content);
        out.flush();
    }

    private void sendNotFoundResponse(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}