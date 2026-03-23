package ru.shift.services;

import ru.shift.view.GameType;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class HighScoresService {
    private static final Path FILE = Path.of("highscores.properties");
    private static final String SECONDS = "seconds";
    private static final String NAME = "name";
    private static final String HEADER = "Records table (name/seconds)";
    private static final String DIFFICULTY_NOVICE = "novice";
    private static final String DIFFICULTY_MEDIUM = "medium";
    private static final String DIFFICULTY_EXPERT = "expert";

    private final Properties properties = new Properties();

    public HighScoresService() {
        load();
    }

    private static String totalString(GameType t, String suffix) {
        String base = switch (t) {
            case NOVICE -> DIFFICULTY_NOVICE;
            case MEDIUM -> DIFFICULTY_MEDIUM;
            case EXPERT -> DIFFICULTY_EXPERT;
        };
        return base + "." + suffix;
    }

    public String getWinner(GameType t) {
        return properties.getProperty(totalString(t, NAME));
    }

    public Integer getBestSeconds(GameType t) {
        String v = properties.getProperty(totalString(t, SECONDS));
        if (v == null) return null;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void updateIfBetter(GameType t, int seconds, String winnerName) {
        Integer current = getBestSeconds(t);
        if (current == null || seconds < current) {
            properties.setProperty(totalString(t, NAME), winnerName);
            properties.setProperty(totalString(t, SECONDS), Integer.toString(seconds));
            save();
        }
    }

    private void load() {
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                properties.load(in);
            } catch (Exception ignored) {
            }
        }
    }

    private void save() {
        try (OutputStream out = Files.newOutputStream(FILE)) {
            properties.store(out, HEADER);
        } catch (Exception ignored) {
        }
    }

}
