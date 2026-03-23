package ru.shift;

import ru.shift.controller.Controller;
import ru.shift.model.GameModel;
import ru.shift.model.MinerModel;
import ru.shift.services.HighScoresService;
import ru.shift.services.MinerTimer;
import ru.shift.view.GameUIHandler;
import ru.shift.view.MainWindow;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new MinerModel();
            MainWindow view = new MainWindow();
            HighScoresService highScores = new HighScoresService();

            MinerTimer timer = new MinerTimer();

            timer.addTickListener(sec ->
                    SwingUtilities.invokeLater(() -> view.setTimerValue(sec))
            );

            Controller controller = new Controller(model);

            view.setCellListener(controller);
            view.setNewGameMenuAction(e -> controller.onNewGameCreate());

            view.initSettings(controller);
            view.setSettingsMenuAction(e ->
                    view.showSettingsDialog(controller.getCurrentGameType())
            );

            view.setGameUIHandler(new GameUIHandler() {
                @Override
                public void onNewGameRequested() {
                    controller.onNewGameCreate();
                }

                @Override
                public void onExitRequested() {
                    view.exitGame();
                }
            });

            view.setHighScoresService(highScores);
            view.setHighScoresMenuAction(e -> view.showHighScores());

            view.setAboutMenuAction(e -> view.showAboutDialog());
            view.setExitMenuAction(e -> view.exitGame());

            view.setCurrentGameTypeSupplier(controller::getCurrentGameType);

            model.addNewGameListener(view);
            model.addGameStateListener(view);
            model.addGameStateListener(timer);
            model.addCellsChangedListener(ups ->
                    SwingUtilities.invokeLater(() -> view.applyUpdates(ups)));

            controller.onNewGameCreate();
            view.setVisible(true);

        });
    }
}
