package org.blab.drum;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.blab.drum.model.Channel;
import org.blab.drum.model.ChannelGroup;
import org.blab.drum.model.DrumService;
import org.blab.drum.model.VcasService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DrumSceneController implements SceneController {
  @FXML private Label serverStatusLabel;
  @FXML private Circle serverStatusIndicator;
  @FXML private GridPane groupsGrid;

  private final Map<String, Stage> stages = new HashMap<>();
  private final int gridWidth;

  public DrumSceneController(int gridWidth) {
    this.gridWidth = gridWidth;
  }

  public void bindDrumService(DrumService service) {
    bindServer(service);
    bindGroups(service, gridWidth);
  }

  private void bindServer(DrumService service) {
    updateServerIndicator(service);
    service
        .getVcasState()
        .addListener((obs, o, n) -> Platform.runLater(() -> updateServerIndicator(service)));
  }

  private void updateServerIndicator(DrumService service) {
    serverStatusLabel.setText(getServerStateLabel(service.getVcasState().getValue()));
    serverStatusIndicator.setFill(getServerStateColor(service.getVcasState().getValue()));
  }

  private String getServerStateLabel(VcasService.State s) {
    return switch (s) {
      case CONNECTED -> "Connected";
      case DISCONNECTED -> "Disconnected";
    };
  }

  private Color getServerStateColor(VcasService.State s) {
    return switch (s) {
      case CONNECTED -> Color.web(Drum.COLOR_GREEN);
      case DISCONNECTED -> Color.web(Drum.COLOR_BLUE);
    };
  }

  private void bindGroups(DrumService service, int gridWidth) {
    int c = 0;
    int r = 0;

    for (var entry : service.getGroups().entrySet()) {
      if (groupsGrid.getRowCount() < r + 1) {
        groupsGrid.addRow(r);

        var constraints = new RowConstraints();
        constraints.setPercentHeight(100.0 / (double) (service.getGroups().size() / gridWidth));
        groupsGrid.getRowConstraints().add(constraints);
      }

      if (groupsGrid.getColumnCount() < c + 1) {
        groupsGrid.addColumn(c);

        var constraints = new ColumnConstraints();
        constraints.setPercentWidth(100.0 / gridWidth);
        groupsGrid.getColumnConstraints().add(constraints);
      }

      groupsGrid.add(wrapButton(createButtonForGroup(entry.getValue())), c, r);

      if (c + 1 == gridWidth) {
        c = 0;
        r++;
      } else c++;
    }
  }

  private AnchorPane wrapButton(Button button) {
    var pane = new AnchorPane(button);

    AnchorPane.setLeftAnchor(button, 0.0);
    AnchorPane.setRightAnchor(button, 0.0);
    AnchorPane.setBottomAnchor(button, 0.0);
    AnchorPane.setTopAnchor(button, 0.0);

    return pane;
  }

  private Button createButtonForGroup(ChannelGroup group) {
    var button = new Button();

    button.setText(group.getName());
    button.setTextFill(Color.WHITE);
    button.setFont(Font.font(16));
    button.setBorder(Border.stroke(Color.BLACK));

    bindButton(button, group);

    return button;
  }

  private void bindButton(Button button, ChannelGroup group) {
    updateButton(button, group);

    group
        .getState()
        .addListener((obs, o, n) -> Platform.runLater(() -> updateButton(button, group)));

    button.setOnMouseClicked(
        (event) -> {
          if (stages.containsKey(group.getName())) stages.get(group.getName()).toFront();
          else createStageForGroup(group);
        });
  }

  private void createStageForGroup(ChannelGroup group) {
    var stage = new Stage();

    stage.setTitle("Drum - " + group.getName());
    stage.setScene(SceneController.newGroupScene(stage, group.getName()));
    stage.setOnCloseRequest((e) -> stages.remove(group.getName()));

    stages.put(group.getName(), stage);

    stage.show();
  }

  private void updateButton(Button button, ChannelGroup group) {
    String title =
        button.getText().contains("(")
            ? button.getText().substring(0, button.getText().indexOf("(")).strip()
            : button.getText().strip();

    button.setText(String.format("%s %s", title, getGroupReason(group)));
    button.setStyle(getGroupStyle(group));
  }

  private String getGroupReason(ChannelGroup group) {
    return switch (group.getState().getValue()) {
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
    return switch (group.getState().getValue()) {
      case NORMAL -> String.format("-fx-background-color: %s;", Drum.COLOR_GREEN);
      case CRITICAL -> String.format("-fx-background-color: %s;", Drum.COLOR_RED);
      case IDLE -> String.format("-fx-background-color: %s;", Drum.COLOR_BLUE);
    };
  }
}
