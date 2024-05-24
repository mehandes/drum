package org.blab.drum;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.drum.model.Channel;
import org.blab.drum.model.ChannelGroup;
import org.blab.drum.model.DrumService;
import org.blab.drum.model.VcasService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DrumSceneController implements SceneController {
  private static final Logger logger = LogManager.getLogger(DrumSceneController.class);

  @FXML private Label vcasStatusLabel;
  @FXML private Circle vcasStatusIndicator;
  @FXML private GridPane grid;

  private final Map<String, Stage> stages = new HashMap<>();

  public void bindDrumService(DrumService service) {
    bindVcasIndicators(service);
    bindButtons(service);
  }

  private void bindVcasIndicators(DrumService service) {
    updateVcasIndicator(service);
    service
        .getObservableVcasState()
        .addListener((obs, o, n) -> Platform.runLater(() -> updateVcasIndicator(service)));

    logger.debug("VCAS indicators bounded");
  }

  private void bindButtons(DrumService service) {
    service
        .getGroups()
        .forEach((groupName, group) -> bindButton((Button) grid.lookup("#" + groupName), group));
  }

  private void bindButton(Button button, ChannelGroup group) {
    updateButton(button, group);
    group
        .getObservableState()
        .addListener((obs, o, n) -> Platform.runLater(() -> updateButton(button, group)));
    button.setOnMouseClicked(
        (event) -> {
          try {
            if (stages.containsKey(group.getName())) stages.get(group.getName()).toFront();
            else {
              Stage stage = new Stage();
              stages.put(group.getName(), stage);
              stage.setTitle("Drum - " + group.getName());
              stage.setScene(SceneController.newGroupScene(stage, group));
              stage.setOnCloseRequest((e) -> stages.remove(group.getName()));
              stage.show();
            }
          } catch (IOException e) {
            logger.fatal(e);
          }
        });

    logger.debug("Button bounded: {}", group.getName());
  }

  private void updateVcasIndicator(DrumService service) {
    vcasStatusLabel.setText(getVcasStateLabel(service.getObservableVcasState().getValue()));
    vcasStatusIndicator.setFill(getVcasStateColor(service.getObservableVcasState().getValue()));
  }

  private String getVcasStateLabel(VcasService.State s) {
    return switch (s) {
      case CONNECTED -> "Connected";
      case DISCONNECTED -> "Disconnected";
    };
  }

  private Color getVcasStateColor(VcasService.State s) {
    return switch (s) {
      case CONNECTED -> Color.web(Drum.COLOR_GREEN);
      case DISCONNECTED -> Color.web(Drum.COLOR_BLUE);
    };
  }

  private void updateButton(Button button, ChannelGroup group) {
    String title =
        button.getText().contains("(")
            ? button.getText().substring(0, button.getText().indexOf("(")).strip()
            : button.getText();

    button.setText(String.format("%s %s", title, getGroupReason(group)));
    button.setStyle(getGroupStyle(group));
  }

  private String getGroupReason(ChannelGroup group) {
    return switch (group.getObservableState().getValue()) {
      case NORMAL -> "";
      case CRITICAL ->
          String.format(
              "(%s)",
              group.getCriticalChannels().stream()
                  .map(Channel::getName)
                  .collect(Collectors.joining(", ")));
      case IDLE ->
          String.format(
              "(%s)",
              group.getIdleChannels().stream()
                  .map(Channel::getName)
                  .collect(Collectors.joining(", ")));
    };
  }

  private String getGroupStyle(ChannelGroup group) {
    return switch (group.getObservableState().getValue()) {
      case NORMAL -> String.format("-fx-background-color: %s;", Drum.COLOR_GREEN);
      case CRITICAL -> String.format("-fx-background-color: %s;", Drum.COLOR_RED);
      case IDLE -> String.format("-fx-background-color: %s;", Drum.COLOR_BLUE);
    };
  }
}
