package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService threadPool;
    private final Map<String, Map<String, Handler>> handlers;

    public Server() {
        this.threadPool = Executors.newFixedThreadPool(64);
        this.handlers = new ConcurrentHashMap<>();
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                .put(path, handler);
    }

    public void listen(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
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
        try (socket;
             InputStream input = socket.getInputStream();
             BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = Request.fromInputStream(input);
            if (request == null) {
                sendBadRequestResponse(output);
                return;
            }

            Handler handler = findHandler(request.getMethod(), request.getPath());
            if (handler != null) {
                handler.handle(request, output);
            } else {
                sendNotFoundResponse(output);
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private Handler findHandler(String method, String path) {
        Map<String, Handler> methodHandlers = handlers.get(method);
        return methodHandlers != null ? methodHandlers.get(path) : null;
    }

    private void sendBadRequestResponse(BufferedOutputStream output) throws IOException {
        output.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        output.flush();
    }

    private void sendNotFoundResponse(BufferedOutputStream output) throws IOException {
        output.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        output.flush();
    }
}