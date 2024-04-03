import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerGUI extends JFrame {
    private HTTPServer server;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;

    public ServerGUI(HTTPServer server) {
        this.server = server;

        setTitle("HTTP WebServer");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        startButton = new JButton("Start Server");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                server.start();
                statusLabel.setText("Server running on port " + server.getPort());
            }
        });

        stopButton = new JButton("Stop Server");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                server.stop();
                statusLabel.setText("Server stopped.");
            }
        });

        statusLabel = new JLabel("Server not running");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(statusLabel);

        add(buttonPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        int port = 80; // Default port for HTTP
        String rootDirectory = "C:\\Codes\\MyServer"; // Change this to your root directory
        HTTPServer server = new HTTPServer(port, rootDirectory);
        ServerGUI gui = new ServerGUI(server);
        gui.setVisible(true);
    }
}
