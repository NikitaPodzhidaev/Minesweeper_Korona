package ru.shift.view;

import ru.shift.model.CellState;
import ru.shift.model.CellUpdate;
import ru.shift.model.GameConfig;
import ru.shift.model.GameState;
import ru.shift.model.GameStateListener;
import ru.shift.model.NewGameListener;
import ru.shift.services.HighScoresService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Supplier;

public class MainWindow extends JFrame implements NewGameListener, GameStateListener {

    private final Container contentPane;
    private final GridBagLayout mainLayout;

    private final SettingsWindow settingsWindow;
    private final WinWindow winWindow;
    private final LoseWindow loseWindow;
    private final HighScoresWindow highScoresWindow;
    private final RecordsWindow recordsWindow;
    private final AboutWindow aboutWindow;

    private JMenuItem newGameMenu;
    private JMenuItem highScoresMenu;
    private JMenuItem settingsMenu;
    private JMenuItem exitMenu;
    private JMenuItem aboutMenu;

    private int totalMines = 0;
    private int flags = 0;
    private CellState[][] stateMirror;

    private CellEventListener cellListener;

    private JButton[][] cellButtons;
    private JLabel timerLabel;
    private JLabel bombsCounterLabel;

    private GameUIHandler gameUIHandler;
    private HighScoresService highScores;
    private Supplier<GameType> currentGameTypeSupplier;

    private int lastSeconds = 0;

    public MainWindow() {
        super("Miner");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        settingsWindow = new SettingsWindow(this);
        winWindow = new WinWindow(this);
        loseWindow = new LoseWindow(this);
        highScoresWindow = new HighScoresWindow(this);
        recordsWindow = new RecordsWindow(this);
        aboutWindow = new AboutWindow(this);

        createMenu();

        contentPane = getContentPane();
        mainLayout = new GridBagLayout();
        contentPane.setLayout(mainLayout);
        contentPane.setBackground(new Color(144, 158, 184));
    }

    public void initSettings(GameTypeListener listener) {
        settingsWindow.setGameTypeListener(listener);
    }

    public void setGameUIHandler(GameUIHandler gameUIHandler){
        this.gameUIHandler = gameUIHandler;
    }

    public void setHighScoresService(HighScoresService hs) {
        this.highScores = hs;
    }

    public void setCurrentGameTypeSupplier(Supplier<GameType> supplier) {
        this.currentGameTypeSupplier = supplier;
    }


    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");

        newGameMenu = new JMenuItem("New Game");
        highScoresMenu = new JMenuItem("High Scores");
        settingsMenu = new JMenuItem("Settings");
        aboutMenu = new JMenuItem("About");
        exitMenu = new JMenuItem("Exit");

        gameMenu.add(newGameMenu);
        gameMenu.addSeparator();
        gameMenu.add(highScoresMenu);
        gameMenu.add(settingsMenu);
        gameMenu.add(aboutMenu);
        gameMenu.addSeparator();
        gameMenu.add(exitMenu);

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    public void setNewGameMenuAction(ActionListener l)     { newGameMenu.addActionListener(l); }
    public void setHighScoresMenuAction(ActionListener l)  { highScoresMenu.addActionListener(l); }
    public void setSettingsMenuAction(ActionListener l)    { settingsMenu.addActionListener(l); }
    public void setAboutMenuAction(ActionListener l)       { aboutMenu.addActionListener(l); }
    public void setExitMenuAction(ActionListener l)        { exitMenu.addActionListener(l); }


    public void setCellListener(CellEventListener listener) {
        this.cellListener = listener;
    }

    public void setCellImage(int x, int y, GameImage gameImage) {
        cellButtons[y][x].setIcon(gameImage.getImageIcon());
    }

    public void setBombsCount(int n) {
        bombsCounterLabel.setText(Integer.toString(n));
    }

    public void setTimerValue(int value) {
        lastSeconds = value;
        timerLabel.setText(String.valueOf(value));
    }

    public void applyUpdates(List<CellUpdate> updates) {
        for (CellUpdate u : updates) {
            GameImage img = switch (u.cellState()) {
                case CLOSED  -> GameImage.CLOSED;
                case FLAGGED -> GameImage.MARKED;
                case OPEN    -> u.isMine() ? GameImage.BOMB : numberToImage(u.neighbourMines());
            };
            setCellImage(u.column(), u.row(), img);

            CellState prev = stateMirror[u.row()][u.column()];
            CellState next = u.cellState();
            if (prev != CellState.FLAGGED && next == CellState.FLAGGED) {
                flags++;
            } else if (prev == CellState.FLAGGED && next != CellState.FLAGGED) {
                flags = Math.max(0, flags - 1);
            }
            stateMirror[u.row()][u.column()] = next;
        }
        int remaining = Math.max(0, totalMines - flags);
        setBombsCount(remaining);
    }

    public void createGameField(int rowsCount, int colsCount) {
        contentPane.removeAll();
        setPreferredSize(new Dimension(20 * colsCount + 70, 20 * rowsCount + 110));

        stateMirror = new CellState[rowsCount][colsCount];
        for (int r = 0; r < rowsCount; r++) {
            for (int c = 0; c < colsCount; c++) {
                stateMirror[r][c] = CellState.CLOSED;
            }
        }
        flags = 0;

        addButtonsPanel(createButtonsPanel(rowsCount, colsCount));
        addTimerImage();
        addTimerLabel(timerLabel = new JLabel("0"));
        addBombCounter(bombsCounterLabel = new JLabel("0"));
        addBombCounterImage();
        pack();
        setLocationRelativeTo(null);
    }

    public void setTotalMines(int total) {
        this.totalMines = Math.max(0, total);
        this.flags = 0;
        setBombsCount(this.totalMines);
    }

    private JPanel createButtonsPanel(int numberOfRows, int numberOfCols) {
        cellButtons = new JButton[numberOfRows][numberOfCols];
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setPreferredSize(new Dimension(20 * numberOfCols, 20 * numberOfRows));
        buttonsPanel.setLayout(new GridLayout(numberOfRows, numberOfCols, 0, 0));

        for (int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < numberOfCols; col++) {
                final int x = col;
                final int y = row;

                JButton btn = new JButton(GameImage.CLOSED.getImageIcon());
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (cellListener == null) return;
                        switch (e.getButton()) {
                            case MouseEvent.BUTTON1 -> cellListener.onMouseClick(x, y, ButtonType.LEFT_BUTTON);
                            case MouseEvent.BUTTON2 -> cellListener.onMouseClick(x, y, ButtonType.MIDDLE_BUTTON);
                            case MouseEvent.BUTTON3 -> cellListener.onMouseClick(x, y, ButtonType.RIGHT_BUTTON);
                            default -> { }
                        }
                    }
                });
                cellButtons[y][x] = btn;
                buttonsPanel.add(btn);
            }
        }

        return buttonsPanel;
    }

    private void addButtonsPanel(JPanel buttonsPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.gridheight = 1;
        gbc.insets = new Insets(20, 20, 5, 20);
        mainLayout.setConstraints(buttonsPanel, gbc);
        contentPane.add(buttonsPanel);
    }

    private void addTimerImage() {
        JLabel label = new JLabel(GameImage.TIMER.getImageIcon());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 20, 0, 0);
        gbc.weightx = 0.1;
        mainLayout.setConstraints(label, gbc);
        contentPane.add(label);
    }

    private void addTimerLabel(JLabel timerLabel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 5, 0, 0);
        mainLayout.setConstraints(timerLabel, gbc);
        contentPane.add(timerLabel);
    }

    private void addBombCounter(JLabel bombsCounterLabel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 0.7;
        mainLayout.setConstraints(bombsCounterLabel, gbc);
        contentPane.add(bombsCounterLabel);
    }

    private void addBombCounterImage() {
        JLabel label = new JLabel(GameImage.BOMB_ICON.getImageIcon());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 5, 0, 20);
        gbc.weightx = 0.1;
        mainLayout.setConstraints(label, gbc);
        contentPane.add(label);
    }


    public void showSettingsDialog(GameType currentGameType) {
        settingsWindow.setGameType(currentGameType);
        settingsWindow.setVisible(true);
    }

    public void showWinDialog(int seconds) {
        winWindow.setTimeSeconds(seconds);
        winWindow.setNewGameListener(e -> {
            if (gameUIHandler != null) {
                gameUIHandler.onNewGameRequested();
            }
        });
        winWindow.setExitListener(e -> {
            if (gameUIHandler != null) {
                gameUIHandler.onExitRequested();
            }
        });
        winWindow.setVisible(true);
        winWindow.toFront();
    }

    public void showLoseDialog() {
        loseWindow.setNewGameListener(e -> {
            if (gameUIHandler != null) {
                gameUIHandler.onNewGameRequested();
            }
        });
        loseWindow.setExitListener(e -> {
            if (gameUIHandler != null) {
                gameUIHandler.onExitRequested();
            }
        });
        loseWindow.setVisible(true);
        loseWindow.toFront();
    }


    public void showHighScores() {
        if (highScores == null) return;

        String nName = highScores.getWinner(GameType.NOVICE);
        Integer nSec = highScores.getBestSeconds(GameType.NOVICE);
        String mName = highScores.getWinner(GameType.MEDIUM);
        Integer mSec = highScores.getBestSeconds(GameType.MEDIUM);
        String xName = highScores.getWinner(GameType.EXPERT);
        Integer xSec = highScores.getBestSeconds(GameType.EXPERT);

        if (nName != null && nSec != null) highScoresWindow.setNoviceRecord(nName, nSec);
        if (mName != null && mSec != null) highScoresWindow.setMediumRecord(mName, mSec);
        if (xName != null && xSec != null) highScoresWindow.setExpertRecord(xName, xSec);

        highScoresWindow.setVisible(true);
        highScoresWindow.toFront();
    }

    public void showRecordsWindow(RecordNameListener recordNameListener) {
        recordsWindow.setNameListener(recordNameListener);
        recordsWindow.setVisible(true);
        recordsWindow.toFront();
    }

    public void showAboutDialog() {
        aboutWindow.setVisible(true);
        aboutWindow.toFront();
    }

    public void exitGame() {
        dispose();
    }

    @Override
    public void onNewGame(GameConfig cfg) {
        SwingUtilities.invokeLater(() -> {
            createGameField(cfg.rows(), cfg.cols());
            setTotalMines(cfg.mines());
        });
    }

    @Override
    public void onGameStateChanged(GameState state) {
        switch (state) {
            case LOST -> SwingUtilities.invokeLater(this::showLoseDialog);
            case WON  -> SwingUtilities.invokeLater(this::handleWin);
            case INIT, PLAY -> { }
        }
    }

    private void handleWin() {
        int secs = lastSeconds;

        if (highScores != null && currentGameTypeSupplier != null) {
            GameType type = currentGameTypeSupplier.get();
            Integer best = highScores.getBestSeconds(type);
            boolean isRecord = (best == null) || (secs < best);

            if (isRecord) {
                showRecordsWindow(name -> {
                    String safe = (name == null || name.isBlank()) ? "Unknown" : name.trim();
                    highScores.updateIfBetter(type, secs, safe);
                    showWinDialog(secs);
                });
                return;
            }
        }

        showWinDialog(secs);
    }


    private static GameImage numberToImage(int n) {
        return switch (n) {
            case 0 -> GameImage.EMPTY;
            case 1 -> GameImage.NUM_1;
            case 2 -> GameImage.NUM_2;
            case 3 -> GameImage.NUM_3;
            case 4 -> GameImage.NUM_4;
            case 5 -> GameImage.NUM_5;
            case 6 -> GameImage.NUM_6;
            case 7 -> GameImage.NUM_7;
            case 8 -> GameImage.NUM_8;
            default -> GameImage.EMPTY;
        };
    }
}
