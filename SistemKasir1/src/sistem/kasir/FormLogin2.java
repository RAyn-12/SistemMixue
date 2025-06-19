package sistem.kasir;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FormLogin2 extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnExit;
    private static final Color MIXUE_RED = new Color(213, 0, 0);
    private static final Color MIXUE_WHITE = Color.WHITE;
    private static final Color MIXUE_YELLOW = new Color(255, 193, 7);
    private static final Color MIXUE_BLACK = Color.BLACK;
    private static final Color MIXUE_GRAY = new Color(245, 245, 245);

    public FormLogin2() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Mixue Cashier System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel content = new JPanel(new GridLayout(4, 2, 10, 10));
        content.setBackground(MIXUE_WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(new JLabel("Username:"));
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        content.add(txtUsername);

        content.add(new JLabel("Password:"));
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createLineBorder(MIXUE_GRAY));
        content.add(txtPassword);

        content.add(new JLabel(""));
        content.add(new JLabel(""));

        btnLogin = createStyledButton("Login", MIXUE_RED, MIXUE_WHITE, MIXUE_YELLOW);
        btnExit = createStyledButton("Keluar", MIXUE_GRAY, MIXUE_BLACK, MIXUE_YELLOW);
        content.add(btnLogin);
        content.add(btnExit);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(content, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> login());
        btnExit.addActionListener(e -> System.exit(0));
    }

    private JButton createStyledButton(String text, Color bg, Color fg, Color hover) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi username dan password!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String roleStr = rs.getString("role"); // Handle ENUM as string
                int role = roleStr.equals("admin") ? 1 : 2; // Map to int
                new TampilanDashboard(username, role).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username atau password salah!", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }
}