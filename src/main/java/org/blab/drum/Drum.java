package org.blab.drum;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.blab.drum.model.Datasource;
import org.blab.drum.model.DatasourceConfiguration;

import java.io.IOException;

public class Drum extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    Datasource.createInstance(new DatasourceConfiguration(null, null, 0));

    FXMLLoader loader = new FXMLLoader(Drum.class.getResource("main-scene.fxml"));
    stage.setTitle("Drum");
    stage.setScene(new Scene(loader.load(), 320, 240));
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
