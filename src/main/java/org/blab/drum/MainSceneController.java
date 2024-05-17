package org.blab.drum;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.blab.drum.model.DrumService;
import org.blab.drum.model.VcasService;

public class MainSceneController {
  @FXML private Label vcasStatusLabel;
  @FXML private Circle vcasStatusIndicator;

  public void setDrumService(DrumService service) {
    vcasStatusLabel.setText(mapVcasStateToLabel(service.getObservableVcasState().getValue()));
    vcasStatusIndicator.setFill(mapVcasStateToColor(service.getObservableVcasState().getValue()));

    service
        .getObservableVcasState()
        .addListener(
            (obs, o, n) -> {
              Platform.runLater(
                  () -> {
                    vcasStatusLabel.setText(
                        mapVcasStateToLabel(service.getObservableVcasState().getValue()));
                    vcasStatusIndicator.setFill(
                        mapVcasStateToColor(service.getObservableVcasState().getValue()));
                  });
            });
  }

  private String mapVcasStateToLabel(VcasService.State s) {
    return switch (s) {
      case CONNECTED -> "Connected";
      case DISCONNECTED -> "Disconnected";
    };
  }

  private Color mapVcasStateToColor(VcasService.State s) {
    return switch (s) {
      case CONNECTED -> Color.GREEN;
      case DISCONNECTED -> Color.RED;
    };
  }
}
