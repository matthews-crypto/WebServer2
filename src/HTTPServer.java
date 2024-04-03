import java.io.*;
import java.net.*;
import javax.script.*;

public class HTTPServer {
    private ServerSocket serverSocket;
    private boolean isRunning;
    private String rootDirectory;
    private int port;

    public HTTPServer(int port, String rootDirectory) {
        try {
            serverSocket = new ServerSocket(port);
            this.port = port; // Initialisation de la variable de port
            this.rootDirectory = rootDirectory;
            isRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
        }
    }

    public int getPort() { // Définition de la méthode getPort() pour récupérer le port
        return port;
    }
    public void start() {
        System.out.println("Server started. Listening on port " + serverSocket.getLocalPort() + "...");
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        // Read the request line
        String requestLine = reader.readLine();
        if (requestLine != null) {
            System.out.println("Received request: " + requestLine);

            // Parse the request
            String[] requestParts = requestLine.split("\\s+");
            if (requestParts.length != 3) {
                sendErrorResponse(writer, 400, "Bad Request");
                return;
            }
            String method = requestParts[0];
            String requestedFile = requestParts[1];

            if (method.equals("GET")) {
                if (requestedFile.endsWith(".py")) {
                    executePythonScript(requestedFile, writer);
                } else {
                    serveFile(requestedFile, writer);
                }
            } else {
                sendErrorResponse(writer, 501, "Not Implemented");
            }
        }

        // Close resources
        reader.close();
        writer.close();
        clientSocket.close();
    }

    private void serveFile(String requestedFile, BufferedWriter writer) throws IOException {
        File file = new File(rootDirectory + requestedFile);
        if (file.exists() && file.isFile()) {
            // Send the file content as response
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            writer.write("HTTP/1.1 200 OK\r\n\r\n");
            String line;
            while ((line = fileReader.readLine()) != null) {
                writer.write(line + "\r\n");
            }
            fileReader.close();
        } else if (file.exists() && file.isDirectory()) {
            // If the requested resource is a directory, send directory listing
            sendDirectoryListing(file, writer);
        } else {
            // If the requested resource does not exist, send 404 Not Found response
            sendErrorResponse(writer, 404, "Not Found");
        }
        writer.flush();
    }

    private void sendDirectoryListing(File directory, BufferedWriter writer) throws IOException {
        File[] files = directory.listFiles();
        writer.write("HTTP/1.1 200 OK\r\n\r\n");
        writer.write("<h1>Directory Listing</h1>\r\n");
        for (File file : files) {
            writer.write("<a href='" + file.getName() + "'>" + file.getName() + "</a><br/>\r\n");
        }
    }

    private void executePythonScript(String requestedFile, BufferedWriter writer) throws IOException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("python");
        try {
            engine.eval(new FileReader(rootDirectory + requestedFile));
            writer.write("HTTP/1.1 200 OK\r\n\r\n");
        } catch (ScriptException e) {
            sendErrorResponse(writer, 500, "Internal Server Error");
        }
    }

    private void sendErrorResponse(BufferedWriter writer, int statusCode, String statusMessage) throws IOException {
        writer.write("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n\r\n");
        writer.write("<h1>" + statusCode + " " + statusMessage + "</h1>\r\n");
    }

    public void stop() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server stopped.");
    }
}
