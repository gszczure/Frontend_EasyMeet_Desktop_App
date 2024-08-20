package pl.meetingapp.frontendtest.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

// klasa z metoda ulatwiajaca w controllerach pisanie polaczen do endpointow
public class HttpUtils {

    public static HttpURLConnection createConnection(String urlString, String requestMethod, String jwtToken, boolean doOutput) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(requestMethod);
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        conn.setRequestProperty("Accept", "application/json");
        if (doOutput) {
            conn.setDoOutput(true);
        }
        return conn;
    }
}
