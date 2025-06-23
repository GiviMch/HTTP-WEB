package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final String cleanPath;
    private final List<NameValuePair> queryParams;

    public Request(String method, String path, Map<String, String> headers, String body)
            throws URISyntaxException {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;

        URI uri = new URI("http://localhost" + path);
        this.cleanPath = uri.getPath();
        this.queryParams = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
    }

    public String getQueryParam(String name) {
        return queryParams.stream()
                .filter(param -> param.getName().equals(name))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElse(null);
    }

    public List<NameValuePair> getQueryParams() {
        return Collections.unmodifiableList(queryParams);
    }



    public static Request fromInputStream(InputStream inputStream) throws IOException, URISyntaxException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read request line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            return null;
        }

        String method = parts[0];
        String path = parts[1];

        // Read headers
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(":", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        // Read body if present
        StringBuilder body = new StringBuilder();
        while (reader.ready()) {
            body.append((char) reader.read());
        }

        return new Request(method, path, headers, body.toString());
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return cleanPath;
    }

    public String getFullPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}