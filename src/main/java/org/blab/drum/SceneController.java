package org.blab.drum;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.blab.drum.model.DrumService;

import java.io.IOException;

public interface SceneController {
  static Scene newDrumScene(Stage stage, int gridWidth) throws IOException {
    FXMLLoader loader = new FXMLLoader(SceneController.class.getResource("drum.fxml"));
    SceneController controller = new DrumSceneController(gridWidth);

    loader.setController(controller);
    stage.setOnShowing((event -> controller.bindDrumService(DrumService.getInstance())));

    return new Scene(loader.load(), 500, 500);
  }

  static Scene newGroupScene(Stage stage, String groupName) {
    FXMLLoader loader = new FXMLLoader(SceneController.class.getResource("group.fxml"));
    SceneController controller = new GroupSceneController(groupName);

    loader.setController(controller);
    stage.setOnShowing((event -> controller.bindDrumService(DrumService.getInstance())));

    try {
      return new Scene(loader.load(), 500, 500);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void bindDrumService(DrumService drumService);
}
