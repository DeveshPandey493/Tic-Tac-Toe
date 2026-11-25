import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * GameUI.java  — v2
 * Full Swing UI for the Tic-Tac-Toe game.
 *
 * Changes over v1:
 *  • Window enlarged to ~1000×860 px
 *  • Board cells enlarged to ~160 px each
 *  • Symbol font increased to 100 px; drawn via fillRect strokes for X
 *    and arc for O so they look bold and crisp at any cell size
 *  • Undo / Redo buttons added (snapshot-based, using Game.Snapshot)
 *  • Button row: [ ↶ Undo ] [ ↷ Redo ] [ ▶ New Game ] [ ↻ Reset Scores ]
 *  • All existing features preserved unchanged
 */
public class GameUI extends JFrame {

    // ═══════════════════════════════════════════════════════════════════════════
    //  Colour palette  (unchanged from v1)
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Color BG_DARK        = new Color(15,  17,  26);
    private static final Color BG_PANEL       = new Color(22,  27,  44);
    private static final Color BG_CELL        = new Color(30,  36,  60);
    private static final Color BG_CELL_HOVER  = new Color(42,  52,  88);
    private static final Color COLOR_X        = new Color(99,  202, 183);  // teal
    private static final Color COLOR_O        = new Color(255, 107, 107);  // coral
    private static final Color COLOR_WIN      = new Color(255, 215,   0);  // gold
    private static final Color COLOR_ACCENT   = new Color(130, 100, 255);  // purple
    private static final Color TEXT_PRIMARY   = new Color(230, 235, 255);
    private static final Color TEXT_MUTED     = new Color(130, 140, 170);
    private static final Color BTN_NEW        = new Color(70,  175, 130);
    private static final Color BTN_RESET      = new Color(195,  70,  70);
    private static final Color BTN_UNDO       = new Color(80,  100, 170);
    private static final Color BTN_REDO       = new Color(100,  80, 170);
    private static final Color BTN_DISABLED   = new Color(45,  50,  70);

    // ═══════════════════════════════════════════════════════════════════════════
    //  Fonts
    // ═══════════════════════════════════════════════════════════════════════════
    private static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  34);
    private static final Font FONT_SCORE_NUM = new Font("Segoe UI", Font.BOLD,  40);
    private static final Font FONT_SCORE_LBL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TURN      = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_BTN       = new Font("Segoe UI", Font.BOLD,  14);

    // ═══════════════════════════════════════════════════════════════════════════
    //  Fields
    // ═══════════════════════════════════════════════════════════════════════════
    private final Game    game;
    private CellButton[]  cells;

    // Labels
    private JLabel        turnLabel;
    private JLabel        scoreXLabel, scoreOLabel, scoreDrawLabel;
    private JLabel        playerXIndicator, playerOIndicator;

    // Buttons
    private RoundedButton undoBtn, redoBtn, newGameBtn, resetBtn;

    // ── Undo / Redo stacks ────────────────────────────────────────────────────
    private final Deque<Game.Snapshot> undoStack = new ArrayDeque<>();
    private final Deque<Game.Snapshot> redoStack = new ArrayDeque<>();

    // ═══════════════════════════════════════════════════════════════════════════
    //  Constructor
    // ═══════════════════════════════════════════════════════════════════════════
    public GameUI() {
        game = new Game();
        setupWindow();
        buildUI();
        refreshUI();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Window setup
    // ═══════════════════════════════════════════════════════════════════════════
    private void setupWindow() {
        setTitle("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 950);
        setMinimumSize(new Dimension(860, 760));
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UI construction
    // ═══════════════════════════════════════════════════════════════════════════
    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(28, 30, 10, 30));

        JLabel title = new JLabel("Tic-Tac-Toe", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        JLabel decoration = new JLabel(createGradientLine(), SwingConstants.CENTER);
        decoration.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(title,      BorderLayout.CENTER);
        titlePanel.add(decoration, BorderLayout.SOUTH);

        header.add(titlePanel, BorderLayout.CENTER);
        return header;
    }

    // ── Center: scoreboard + board ────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setBackground(BG_DARK);
        center.setBorder(new EmptyBorder(0, 36, 0, 36));
        center.add(buildScoreboard(), BorderLayout.NORTH);
        center.add(buildBoard(),      BorderLayout.CENTER);
        return center;
    }

    // ── Scoreboard ────────────────────────────────────────────────────────────
    private JPanel buildScoreboard() {
        JPanel score = new JPanel(new GridLayout(1, 3, 16, 0));
        score.setBackground(BG_DARK);
        score.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel cardX = buildScoreCard("Player X", "0", COLOR_X, true);
        scoreXLabel      = findScoreLabel(cardX);
        playerXIndicator = findIndicator(cardX);

        JPanel cardD = buildScoreCard("Draws", "0", TEXT_MUTED, false);
        scoreDrawLabel = findScoreLabel(cardD);

        JPanel cardO = buildScoreCard("Player O", "0", COLOR_O, false);
        scoreOLabel      = findScoreLabel(cardO);
        playerOIndicator = findIndicator(cardO);

        score.add(cardX);
        score.add(cardD);
        score.add(cardO);
        return score;
    }

    private JPanel buildScoreCard(String playerName, String scoreVal, Color accent, boolean active) {
        RoundedPanel card = new RoundedPanel(18, BG_PANEL);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(14, 10, 14, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        top.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dot.setForeground(active ? accent : new Color(60, 65, 90));
        dot.setName("indicator");
        JLabel name = new JLabel(playerName);
        name.setFont(FONT_SCORE_LBL);
        name.setForeground(TEXT_MUTED);
        top.add(dot);
        top.add(name);

        JLabel scoreLabel = new JLabel(scoreVal, SwingConstants.CENTER);
        scoreLabel.setFont(FONT_SCORE_NUM);
        scoreLabel.setForeground(accent);
        scoreLabel.setName("score");

        card.add(top,        BorderLayout.NORTH);
        card.add(scoreLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel findScoreLabel(JPanel card) {
        for (Component c : card.getComponents())
            if (c instanceof JLabel && "score".equals(c.getName())) return (JLabel) c;
        return new JLabel();
    }

    private JLabel findIndicator(JPanel card) {
        for (Component c : card.getComponents())
            if (c instanceof JPanel)
                for (Component inner : ((JPanel) c).getComponents())
                    if (inner instanceof JLabel && "indicator".equals(inner.getName()))
                        return (JLabel) inner;
        return new JLabel();
    }

    // ── Board ─────────────────────────────────────────────────────────────────
    private JPanel buildBoard() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_DARK);

        // Board panel: 3×3 grid with generous padding and gaps
        RoundedPanel board = new RoundedPanel(28, BG_PANEL);
        board.setLayout(new GridLayout(3, 3, 14, 14));
        board.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Target 200 px per cell → board ~ 3*200 + 2*14 + 2*18 = 664 px
        int cellSize  = 200;
        int boardSize = cellSize * 3 + 14 * 2 + 18 * 2;
        board.setPreferredSize(new Dimension(boardSize, boardSize));

        cells = new CellButton[9];
        for (int i = 0; i < 9; i++) {
            cells[i] = new CellButton(i, cellSize);
            cells[i].addActionListener(this::onCellClicked);
            board.add(cells[i]);
        }
        wrapper.add(board);
        return wrapper;
    }

    // ── Footer: turn display + buttons ────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(10, 36, 28, 36));

        // Turn label
        turnLabel = new JLabel("Player X's Turn", SwingConstants.CENTER);
        turnLabel.setFont(FONT_TURN);
        turnLabel.setForeground(COLOR_X);

        // ── Button row: Undo | Redo | New Game | Reset Scores ─────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);

        undoBtn = new RoundedButton("↶  Undo", BTN_UNDO, Color.WHITE, 22);
        undoBtn.setFont(FONT_BTN);
        undoBtn.setPreferredSize(new Dimension(140, 44));
        undoBtn.addActionListener(e -> onUndo());

        redoBtn = new RoundedButton("↷  Redo", BTN_REDO, Color.WHITE, 22);
        redoBtn.setFont(FONT_BTN);
        redoBtn.setPreferredSize(new Dimension(140, 44));
        redoBtn.addActionListener(e -> onRedo());

        newGameBtn = new RoundedButton("▶  New Game", BTN_NEW, Color.WHITE, 22);
        newGameBtn.setFont(FONT_BTN);
        newGameBtn.setPreferredSize(new Dimension(155, 44));
        newGameBtn.addActionListener(e -> onNewGame());

        resetBtn = new RoundedButton("↺  Reset Scores", BTN_RESET, Color.WHITE, 22);
        resetBtn.setFont(FONT_BTN);
        resetBtn.setPreferredSize(new Dimension(165, 44));
        resetBtn.addActionListener(e -> onResetScores());

        btnRow.add(undoBtn);
        btnRow.add(redoBtn);
        btnRow.add(newGameBtn);
        btnRow.add(resetBtn);

        footer.add(turnLabel, BorderLayout.CENTER);
        footer.add(btnRow,    BorderLayout.SOUTH);
        return footer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Game actions
    // ═══════════════════════════════════════════════════════════════════════════

    private void onCellClicked(ActionEvent e) {
        CellButton cell = (CellButton) e.getSource();

        // Save state BEFORE the move (for undo)
        Game.Snapshot before = game.takeSnapshot();

        boolean moved = game.makeMove(cell.getIndex());
        if (!moved) return;

        // A new move clears redo history
        redoStack.clear();
        undoStack.push(before);

        // Animate placed symbol
        cell.animatePlace(game.getCell(cell.getIndex()));

        refreshUI();
        if (game.isGameOver()) handleGameOver();
    }

    private void onUndo() {
        if (undoStack.isEmpty()) return;

        // Push current state onto redo stack
        redoStack.push(game.takeSnapshot());

        // Pop and restore the previous state
        Game.Snapshot prev = undoStack.pop();
        game.restoreSnapshot(prev);

        // Stop all win-pulse timers and hard-reset every cell to its symbol
        for (int i = 0; i < 9; i++) {
            cells[i].forceSymbol(game.getCell(i));
        }
        refreshUI();
    }

    private void onRedo() {
        if (redoStack.isEmpty()) return;

        // Push current state onto undo stack
        undoStack.push(game.takeSnapshot());

        // Pop and restore the redo state
        Game.Snapshot next = redoStack.pop();
        game.restoreSnapshot(next);

        for (int i = 0; i < 9; i++) {
            cells[i].forceSymbol(game.getCell(i));
        }

        // If restoring into a game-over state, re-trigger animations
        if (game.isGameOver()) {
            int[] combo = game.getWinningCombo();
            if (combo != null)
                for (int idx : combo) cells[idx].animateWin();
        }
        refreshUI();
    }

    private void onNewGame() {
        undoStack.clear();
        redoStack.clear();
        game.newGame();
        for (CellButton cell : cells) cell.reset();
        refreshUI();
        animateNewGame();
    }

    private void onResetScores() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Reset all scores to zero?", "Reset Scores",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            undoStack.clear();
            redoStack.clear();
            game.resetAll();
            for (CellButton cell : cells) cell.reset();
            refreshUI();
        }
    }

    private void handleGameOver() {
        int[] combo = game.getWinningCombo();
        if (combo != null) {
            for (int idx : combo) cells[idx].animateWin();
            showPopup(game.getWinner() + " Wins! 🏆",
                      "Congratulations, Player " + game.getWinner() + "!",
                      game.getWinner() == Game.PLAYER_X ? COLOR_X : COLOR_O);
        } else {
            for (CellButton cell : cells) cell.animateDraw();
            showPopup("It's a Draw! 🤝",
                      "Great game — nobody wins this round.",
                      COLOR_ACCENT);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UI refresh
    // ═══════════════════════════════════════════════════════════════════════════
    private void refreshUI() {
        char    current = game.getCurrentPlayer();
        boolean over    = game.isGameOver();

        // Turn label
        if (over) {
            if (game.getWinningCombo() != null) {
                char winner = game.getWinner();
                turnLabel.setText("Player " + winner + " Wins! 🏆");
                turnLabel.setForeground(winner == Game.PLAYER_X ? COLOR_X : COLOR_O);
            } else {
                turnLabel.setText("It's a Draw! 🤝");
                turnLabel.setForeground(COLOR_ACCENT);
            }
        } else {
            turnLabel.setText("Player " + current + "'s Turn");
            turnLabel.setForeground(current == Game.PLAYER_X ? COLOR_X : COLOR_O);
        }

        // Scores
        scoreXLabel.setText(String.valueOf(game.getScoreX()));
        scoreOLabel.setText(String.valueOf(game.getScoreO()));
        scoreDrawLabel.setText(String.valueOf(game.getDraws()));

        // Active player indicator dot
        boolean xActive = !over && current == Game.PLAYER_X;
        boolean oActive = !over && current == Game.PLAYER_O;
        playerXIndicator.setForeground(xActive ? COLOR_X : new Color(60, 65, 90));
        playerOIndicator.setForeground(oActive ? COLOR_O : new Color(60, 65, 90));

        // Cells enabled/disabled
        for (int i = 0; i < 9; i++)
            cells[i].setEnabled(!over && game.getCell(i) == Game.EMPTY);

        // Undo / Redo button states
        undoBtn.setEnabled(!undoStack.isEmpty());
        redoBtn.setEnabled(!redoStack.isEmpty());
        undoBtn.setDisabledColor(BTN_DISABLED);
        redoBtn.setDisabledColor(BTN_DISABLED);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Popup dialog
    // ═══════════════════════════════════════════════════════════════════════════
    private void showPopup(String title, String message, Color accent) {
        Timer delay = new Timer(500, e -> {
            GameOverDialog dialog = new GameOverDialog(this, title, message, accent);
            dialog.setVisible(true);
            if (dialog.wantsNewGame()) onNewGame();
        });
        delay.setRepeats(false);
        delay.start();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  New-game ripple animation
    // ═══════════════════════════════════════════════════════════════════════════
    private void animateNewGame() {
        for (int step = 0; step < 9; step++) {
            final int s = step;
            Timer t = new Timer(step * 50, e -> cells[s].animateFadeIn());
            t.setRepeats(false);
            t.start();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Gradient title decoration
    // ═══════════════════════════════════════════════════════════════════════════
    private ImageIcon createGradientLine() {
        int w = 340, h = 3;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setPaint(new GradientPaint(0, 0, new Color(0,0,0,0), w/2f, 0, COLOR_ACCENT));
        g.fillRect(0, 0, w/2, h);
        g.setPaint(new GradientPaint(w/2f, 0, COLOR_ACCENT, w, 0, new Color(0,0,0,0)));
        g.fillRect(w/2, 0, w/2, h);
        g.dispose();
        return new ImageIcon(img);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ── Inner classes ──────────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * CellButton — a single board cell that draws its own X/O.
     *
     * X is drawn as two thick diagonal strokes (via polygon fill).
     * O is drawn as a thick ring (via two concentric filled arcs).
     * Both look bold and crisp at large sizes without relying on font rendering.
     */
    private class CellButton extends JButton {
        private final int  index;
        private final int  cellSize;
        private char  symbol    = Game.EMPTY;

        // Animation state
        private float scale     = 1f;
        private float alpha     = 1f;

        // Win pulse
        private float   winPulse  = 0f;
        private boolean pulsing   = false;
        private Timer   pulseTimer;

        // Hover
        private float hoverAlpha = 0f;
        private Timer hoverTimer;

        CellButton(int index, int cellSize) {
            this.index    = index;
            this.cellSize = cellSize;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(cellSize, cellSize));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { startHover(true);  }
                @Override public void mouseExited (MouseEvent e) { startHover(false); }
            });
        }

        int getIndex() { return index; }

        // ── State helpers ─────────────────────────────────────────────────────

        /** Hard-sets the symbol without animation (used by undo/redo restore). */
        void forceSymbol(char sym) {
            // Stop any running pulse
            if (pulseTimer != null) { pulseTimer.stop(); pulseTimer = null; }
            pulsing   = false;
            winPulse  = 0f;
            symbol    = sym;
            scale     = 1f;
            alpha     = 1f;
            setEnabled(sym == Game.EMPTY);
            repaint();
        }

        void reset() {
            if (pulseTimer != null) { pulseTimer.stop(); pulseTimer = null; }
            symbol    = Game.EMPTY;
            scale     = 1f;
            alpha     = 1f;
            hoverAlpha = 0f;
            pulsing   = false;
            winPulse  = 0f;
            setEnabled(true);
            repaint();
        }

        // ── Animations ───────────────────────────────────────────────────────

        void animatePlace(char sym) {
            this.symbol = sym;
            animateScale(0.25f, 1.12f, 90, () -> animateScale(1.12f, 1f, 70, null));
        }

        void animateWin() {
            pulsing   = true;
            winPulse  = 0f;
            pulseTimer = new Timer(16, e -> {
                winPulse = (winPulse + 0.07f) % (2 * (float) Math.PI);
                repaint();
            });
            pulseTimer.start();
        }

        void animateDraw() {
            int[] offsets = {-7, 7, -5, 5, -3, 3, 0};
            for (int i = 0; i < offsets.length; i++) {
                final int dx = offsets[i];
                Timer t = new Timer(i * 40, e -> {
                    int pad = Math.abs(dx);
                    setBorder(dx < 0
                            ? new EmptyBorder(0, pad, 0, 0)
                            : new EmptyBorder(0, 0, 0, pad));
                    repaint();
                });
                t.setRepeats(false);
                t.start();
            }
        }

        void animateFadeIn() {
            alpha = 0f;
            int[] step = {0};
            Timer t = new Timer(16, null);
            t.addActionListener(e -> {
                step[0]++;
                alpha = Math.min(1f, step[0] / 14f);
                repaint();
                if (alpha >= 1f) ((Timer) e.getSource()).stop();
            });
            t.start();
        }

        private void animateScale(float from, float to, int durationMs, Runnable onDone) {
            int steps = Math.max(1, durationMs / 16);
            int[] step = {0};
            scale = from;
            Timer t = new Timer(16, null);
            t.addActionListener(e -> {
                step[0]++;
                float progress = (float) step[0] / steps;
                scale = from + (to - from) * easeOutQuad(progress);
                repaint();
                if (step[0] >= steps) {
                    ((Timer) e.getSource()).stop();
                    scale = to;
                    if (onDone != null) onDone.run();
                }
            });
            t.start();
        }

        private void startHover(boolean enter) {
            if (hoverTimer != null) hoverTimer.stop();
            float target = enter ? 1f : 0f;
            hoverTimer = new Timer(16, null);
            hoverTimer.addActionListener(e -> {
                hoverAlpha += (target - hoverAlpha) * 0.18f;
                repaint();
                if (Math.abs(hoverAlpha - target) < 0.01f) {
                    hoverAlpha = target;
                    ((Timer) e.getSource()).stop();
                }
            });
            hoverTimer.start();
        }

        private float easeOutQuad(float t) { return 1 - (1 - t) * (1 - t); }

        // ── Painting ─────────────────────────────────────────────────────────
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth(), h = getHeight();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // ── Background ───────────────────────────────────────────────────
            if (pulsing) {
                float glow   = 0.5f + 0.5f * (float) Math.sin(winPulse);
                Color winBg  = blendColor(BG_CELL, COLOR_WIN, 0.22f + 0.14f * glow);
                drawCellBg(g, w, h, winBg);
                // Glowing border
                g.setColor(new Color(COLOR_WIN.getRed(), COLOR_WIN.getGreen(), COLOR_WIN.getBlue(),
                        (int)(200 * glow)));
                g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 22, 22));
            } else {
                Color bg = blendColor(BG_CELL, BG_CELL_HOVER, hoverAlpha);
                drawCellBg(g, w, h, bg);
            }

            // ── Symbol ───────────────────────────────────────────────────────
            if (symbol != Game.EMPTY) {
                Color symColor = pulsing ? COLOR_WIN
                        : (symbol == Game.PLAYER_X ? COLOR_X : COLOR_O);

                // Apply scale transform around center
                g.translate(w / 2.0, h / 2.0);
                g.scale(scale, scale);

                if (symbol == Game.PLAYER_X) {
                    drawX(g, w, h, symColor);
                } else {
                    drawO(g, w, h, symColor);
                }

                g.scale(1.0 / scale, 1.0 / scale);
                g.translate(-w / 2.0, -h / 2.0);
            }

            g.dispose();
        }

        /**
         * Draws a bold X using two thick rounded strokes.
         * Origin is at the cell centre (0,0) in the transformed coordinate system.
         */
        private void drawX(Graphics2D g, int w, int h, Color color) {
            int half = (int)(Math.min(w, h) * 0.30);   // arm half-length
            float thickness = Math.min(w, h) * 0.115f; // stroke width

            g.setColor(color);
            g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Float(-half, -half,  half,  half));
            g.draw(new Line2D.Float( half, -half, -half,  half));
        }

        /**
         * Draws a bold O using a thick ring (stroke).
         * Origin is at the cell centre (0,0) in the transformed coordinate system.
         */
        private void drawO(Graphics2D g, int w, int h, Color color) {
            int    radius    = (int)(Math.min(w, h) * 0.28);
            float  thickness = Math.min(w, h) * 0.115f;

            g.setColor(color);
            g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Float(-radius, -radius, radius * 2, radius * 2));
        }

        private void drawCellBg(Graphics2D g, int w, int h, Color bg) {
            g.setColor(bg);
            g.fill(new RoundRectangle2D.Float(0, 0, w, h, 22, 22));
        }

        private Color blendColor(Color a, Color b, float t) {
            t = Math.max(0, Math.min(1, t));
            int r  = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
            int gv = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
            int bl = (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
            return new Color(r, gv, bl);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    /** JPanel that paints itself with rounded corners. */
    static class RoundedPanel extends JPanel {
        private final int   arc;
        private final Color bg;

        RoundedPanel(int arc, Color bg) {
            this.arc = arc;
            this.bg  = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(bg);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    /** Pill-shaped button with animated hover and disabled-colour support. */
    static class RoundedButton extends JButton {
        private Color normalBg;
        private Color disabledBg;
        private final int arc;
        private float hoverProgress = 0f;
        private Timer hoverTimer;

        RoundedButton(String text, Color bg, Color fg, int arc) {
            super(text);
            this.normalBg   = bg;
            this.disabledBg = new Color(45, 50, 70);
            this.arc        = arc;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) animateHover(1f); }
                @Override public void mouseExited (MouseEvent e) { animateHover(0f); }
            });
        }

        void setDisabledColor(Color c) { this.disabledBg = c; }

        private void animateHover(float target) {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new Timer(16, e -> {
                hoverProgress += (target - hoverProgress) * 0.18f;
                repaint();
                if (Math.abs(hoverProgress - target) < 0.01f) {
                    hoverProgress = target;
                    ((Timer) e.getSource()).stop();
                }
            });
            hoverTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color base  = isEnabled() ? normalBg : disabledBg;
            Color hover = isEnabled() ? base.brighter() : disabledBg;
            int r  = (int)(base.getRed()   + (hover.getRed()   - base.getRed())   * hoverProgress);
            int gv = (int)(base.getGreen() + (hover.getGreen() - base.getGreen()) * hoverProgress);
            int bl = (int)(base.getBlue()  + (hover.getBlue()  - base.getBlue())  * hoverProgress);

            g.setColor(new Color(r, gv, bl));
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

            if (isEnabled() && hoverProgress > 0.05f) {
                g.setColor(new Color(r, gv, bl, (int)(60 * hoverProgress)));
                g.setStroke(new BasicStroke(3f));
                g.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, arc, arc));
            }

            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    /** Custom game-over dialog. */
    static class GameOverDialog extends JDialog {
        private boolean wantsNewGame = false;

        GameOverDialog(Frame owner, String title, String message, Color accent) {
            super(owner, true);
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));
            setSize(400, 240);
            setLocationRelativeTo(owner);

            JPanel root = new RoundedPanel(26, new Color(22, 27, 44));
            root.setLayout(new BorderLayout(0, 0));
            root.setBorder(new EmptyBorder(32, 34, 26, 34));

            JPanel accentBar = new JPanel();
            accentBar.setBackground(accent);
            accentBar.setPreferredSize(new Dimension(0, 4));

            JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 25));
            titleLbl.setForeground(accent);

            JLabel msgLbl = new JLabel(
                    "<html><div style='text-align:center'>" + message + "</div></html>",
                    SwingConstants.CENTER);
            msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            msgLbl.setForeground(new Color(170, 180, 210));
            msgLbl.setBorder(new EmptyBorder(12, 0, 22, 0));

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
            btnPanel.setOpaque(false);

            RoundedButton newGameBtn = new RoundedButton("New Game", new Color(70, 175, 130), Color.WHITE, 20);
            newGameBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            newGameBtn.setPreferredSize(new Dimension(135, 42));
            newGameBtn.addActionListener(e -> { wantsNewGame = true; dispose(); });

            RoundedButton closeBtn = new RoundedButton("Close", new Color(60, 65, 100), Color.WHITE, 20);
            closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            closeBtn.setPreferredSize(new Dimension(105, 42));
            closeBtn.addActionListener(e -> dispose());

            btnPanel.add(newGameBtn);
            btnPanel.add(closeBtn);

            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);
            content.add(titleLbl, BorderLayout.NORTH);
            content.add(msgLbl,   BorderLayout.CENTER);
            content.add(btnPanel, BorderLayout.SOUTH);

            root.add(accentBar, BorderLayout.NORTH);
            root.add(content,   BorderLayout.CENTER);
            setContentPane(root);
        }

        boolean wantsNewGame() { return wantsNewGame; }
    }
}
