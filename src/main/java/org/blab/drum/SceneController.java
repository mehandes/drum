package org.blab.drum;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.blab.drum.model.ChannelGroup;
import org.blab.drum.model.DrumService;

import java.io.IOException;

public interface SceneController {
  static Scene newDrumScene(Stage stage) throws IOException {
    FXMLLoader loader = new FXMLLoader(SceneController.class.getResource("drum.fxml"));
    DrumSceneController controller = new DrumSceneController();

    loader.setController(controller);
    stage.setOnShowing((event -> controller.bindDrumService(DrumService.getInstance())));

    return new Scene(loader.load(), 500, 500);
  }

  static Scene newGroupScene(Stage stage, ChannelGroup group) throws IOException {
    FXMLLoader loader = new FXMLLoader(SceneController.class.getResource("group.fxml"));
    GroupSceneController controller = new GroupSceneController(group);

    loader.setController(controller);
    stage.setOnShowing((event -> controller.bindDrumService(DrumService.getInstance())));

    return new Scene(loader.load(), 500, 500);
  }

  void bindDrumService(DrumService drumService);
}
