package org.blab.drum;

import javafx.application.Application;
import javafx.stage.Stage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.blab.drum.model.DrumService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Drum extends Application {
  public static final String COLOR_GREEN = "#0EAD69";
  public static final String COLOR_RED = "#A41623";
  public static final String COLOR_BLUE = "#044B7F";

  public static void main(String[] args) {
    configureLogger();
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
    var properties =
        DrumProperties.load(
            new InputStreamReader(
                new FileInputStream(
                    System.getProperty("user.home") + "/.config/drum/drum-default.json")));

    DrumService.init(properties);

    stage.setTitle("Drum");
    stage.setScene(SceneController.newDrumScene(stage, properties.gridWidth()));
    stage.show();
  }

  private static void configureLogger() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

    loggerConfig.setLevel(Level.INFO);
    ctx.updateLoggers();
  }
}
