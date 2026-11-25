/**
 * Game.java
 * Handles all Tic-Tac-Toe game logic: board state, turn management,
 * win/draw detection, scoring, and snapshot-based Undo/Redo.
 *
 * NOTE: The core game logic (makeMove, win/draw detection, scoring) is
 * unchanged from v1. Only the Snapshot inner-class and undo/redo
 * stack helpers have been added.
 */
public class Game {

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final char EMPTY    = ' ';
    public static final char PLAYER_X = 'X';
    public static final char PLAYER_O = 'O';

    /** All 8 winning lines (row-major indices into the 3×3 board). */
    public static final int[][] WIN_COMBOS = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},   // rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},   // columns
        {0, 4, 8}, {2, 4, 6}               // diagonals
    };

    // ── Snapshot (for Undo / Redo) ─────────────────────────────────────────────
    /**
     * Immutable snapshot of the entire transient game state.
     * Scores are NOT included because undo/redo only walks within a single
     * game round; scores only change on game-over and are managed separately.
     */
    public static class Snapshot {
        final char[] board;
        final char   currentPlayer;
        final boolean gameOver;
        final int[]  winningCombo;   // may be null
        final int    scoreX, scoreO, draws;

        Snapshot(char[] board, char currentPlayer, boolean gameOver,
                 int[] winningCombo, int scoreX, int scoreO, int draws) {
            this.board         = board.clone();
            this.currentPlayer = currentPlayer;
            this.gameOver      = gameOver;
            this.winningCombo  = (winningCombo == null) ? null : winningCombo.clone();
            this.scoreX        = scoreX;
            this.scoreO        = scoreO;
            this.draws         = draws;
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────
    private char[]   board;
    private char     currentPlayer;
    private boolean  gameOver;
    private int[]    winningCombo;   // null until someone wins
    private int      scoreX;
    private int      scoreO;
    private int      draws;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Game() {
        scoreX = 0;
        scoreO = 0;
        draws  = 0;
        newGame();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Resets the board for a new round (scores are preserved). */
    public void newGame() {
        board         = new char[9];
        winningCombo  = null;
        gameOver      = false;
        currentPlayer = PLAYER_X;
        for (int i = 0; i < 9; i++) board[i] = EMPTY;
    }

    /**
     * Attempts to place the current player's mark at {@code index}.
     *
     * @param index  0-based cell index (0=top-left … 8=bottom-right)
     * @return {@code true} if the move was legal and the board was updated
     */
    public boolean makeMove(int index) {
        if (gameOver || board[index] != EMPTY) return false;

        board[index] = currentPlayer;
        winningCombo = findWinningCombo();

        if (winningCombo != null) {
            gameOver = true;
            if (currentPlayer == PLAYER_X) scoreX++;
            else                           scoreO++;
        } else if (isBoardFull()) {
            gameOver = true;
            draws++;
        } else {
            currentPlayer = (currentPlayer == PLAYER_X) ? PLAYER_O : PLAYER_X;
        }

        return true;
    }

    /** Resets scores and starts a fresh game. */
    public void resetAll() {
        scoreX = 0;
        scoreO = 0;
        draws  = 0;
        newGame();
    }

    // ── Snapshot API ──────────────────────────────────────────────────────────

    /** Captures the full current state as an immutable snapshot. */
    public Snapshot takeSnapshot() {
        return new Snapshot(board, currentPlayer, gameOver, winningCombo,
                            scoreX, scoreO, draws);
    }

    /** Restores the game to exactly the state captured in {@code snap}. */
    public void restoreSnapshot(Snapshot snap) {
        this.board         = snap.board.clone();
        this.currentPlayer = snap.currentPlayer;
        this.gameOver      = snap.gameOver;
        this.winningCombo  = (snap.winningCombo == null) ? null : snap.winningCombo.clone();
        this.scoreX        = snap.scoreX;
        this.scoreO        = snap.scoreO;
        this.draws         = snap.draws;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public char    getCell(int index)   { return board[index]; }
    public char    getCurrentPlayer()   { return currentPlayer; }
    public boolean isGameOver()         { return gameOver; }
    public int[]   getWinningCombo()    { return winningCombo; }
    public int     getScoreX()          { return scoreX; }
    public int     getScoreO()          { return scoreO; }
    public int     getDraws()           { return draws; }

    /** Returns the winner character, or EMPTY if no winner yet. */
    public char getWinner() {
        return (winningCombo != null) ? board[winningCombo[0]] : EMPTY;
    }

    public boolean isDraw() {
        return gameOver && winningCombo == null;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private int[] findWinningCombo() {
        for (int[] combo : WIN_COMBOS) {
            char a = board[combo[0]], b = board[combo[1]], c = board[combo[2]];
            if (a != EMPTY && a == b && b == c) return combo;
        }
        return null;
    }

    private boolean isBoardFull() {
        for (char cell : board) if (cell == EMPTY) return false;
        return true;
    }
}
