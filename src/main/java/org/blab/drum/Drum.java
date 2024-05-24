package org.blab.drum;

import javafx.application.Application;
import javafx.stage.Stage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.blab.drum.model.DrumProperties;
import org.blab.drum.model.DrumService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

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
        new DrumProperties(
            Set.of(
                "VEPP/QUAD/1D1/MCur.1",
                "VEPP/QUAD/1D1/Volt",
                "VEPP/QUAD/1D2/MCur.1",
                "VEPP/QUAD/1D2/Volt",
                "VEPP/QUAD/1D3/MCur.1",
                "VEPP/QUAD/1D3/Volt",
                "VEPP/QUAD/1F1/MCur.1",
                "VEPP/QUAD/1F1/Volt",
                "VEPP/QUAD/1F2/MCur.1",
                "VEPP/QUAD/1F2/Volt",
                "VEPP/QUAD/1F3/MCur.1",
                "VEPP/QUAD/1F3/Volt"),
            100,
            10,
            new InetSocketAddress("172.16.1.110", 20041));

    DrumService.init(properties);

    stage.setTitle("Drum");
    stage.setScene(SceneController.newDrumScene(stage));
    stage.show();
  }

  private static void configureLogger() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

    loggerConfig.setLevel(Level.ALL);
    ctx.updateLoggers();
  }
}
