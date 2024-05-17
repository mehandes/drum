package org.blab.drum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.blab.drum.model.Datasource;
import org.blab.drum.model.DatasourceProperties;
import org.blab.vcas.consumer.ConsumerProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

public class Drum extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader loader = new FXMLLoader(Drum.class.getResource("main-scene.fxml"));
    stage.setTitle("Drum");
    stage.setScene(new Scene(loader.load(), 320, 240));
    stage.show();
  }

  public static void main(String[] args) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 

    loggerConfig.setLevel(Level.ALL);
    ctx.updateLoggers();

    Datasource.createInstance(new DatasourceProperties(
      Set.of("VEPP/CCD/1M1L/sigma_x", "VEPP/CCD/1M1L/sigma_z"), 100), 
      new ConsumerProperties(new InetSocketAddress("172.16.1.110", 20041), 2048, 1000)
    );

    launch();
  }
}
