package org.blab.drum;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TabPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.drum.model.ChannelGroup;
import org.blab.drum.model.DrumService;
import org.blab.drum.model.ObservableQueue;

public class GroupSceneController implements SceneController {
  private static final Logger logger = LogManager.getLogger(GroupSceneController.class);

  @FXML TabPane tabPane;

  private final ChannelGroup group;

  public GroupSceneController(ChannelGroup group) {
    this.group = group;
  }

  public void bindDrumService(DrumService drumService) {
    tabPane
        .getTabs()
        .forEach(tab -> bindChart((LineChart<String, Number>) tab.getContent().lookup("#chart")));
  }

  private void bindChart(LineChart<String, Number> chart) {
    var channel = group.getChannelByName((String) chart.getUserData());
    var data = new XYChart.Series<String, Number>();

    data.setData(channel.getObservableData().getShadow());
    chart.getData().add(data);

    logger.debug("Chart bounded: {}", channel.getName());
  }
}
