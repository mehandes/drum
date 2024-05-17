package org.blab.drum;

import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.blab.drum.model.DrumProperties;
import org.blab.drum.model.DrumService;
import org.blab.drum.model.Range;
import org.blab.vcas.consumer.ConsumerProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

public class Drum extends Application {
  public static void main(String[] args) {
    configureLogger();
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
    var properties =
        new DrumProperties(
            Set.of("VEPP/CCD/1M1L/sigma_x", "VEPP/CCD/1M1L/sigma_z"),
            100,
            Range.of(0, 100),
            3000,
            new ConsumerProperties(new InetSocketAddress("172.16.1.110", 20041), 2048, 3000));

    var drumService = new DrumService(properties);
    var mainSceneController = new MainSceneController();
    var mainScene = getMainScene(mainSceneController);

    stage.setTitle("Drum");
    stage.setScene(mainScene);
    stage.show();

    mainSceneController.setDrumService(drumService);
  }

  private Scene getMainScene(MainSceneController controller) throws IOException {
    FXMLLoader loader = new FXMLLoader(Drum.class.getResource("main-scene.fxml"));
    loader.setController(controller);
    return new Scene(loader.load(), 500, 500);
  }

  private static void configureLogger() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

    loggerConfig.setLevel(Level.ALL);
    ctx.updateLoggers();
  }
}
