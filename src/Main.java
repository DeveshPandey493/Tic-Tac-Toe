import javax.swing.*;

/**
 * Main.java
 * Entry point. Launches the Tic-Tac-Toe game on the Swing Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        // Apply system-level rendering hints for crisp UI on HiDPI displays
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Run everything on the Event Dispatch Thread (EDT) — the Swing way
        SwingUtilities.invokeLater(() -> {
            // Set the look-and-feel to the system default for native window chrome
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { /* fall back to Metal L&F */ }

            GameUI window = new GameUI();
            window.setVisible(true);
        });
    }
}
