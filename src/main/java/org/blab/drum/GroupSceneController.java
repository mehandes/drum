package org.blab.drum;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TabPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.drum.model.ChannelGroup;
import org.blab.drum.model.DrumService;

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

    data.setData(channel.getData().getShadow());
    chart.getData().add(data);

    data.getData()
        .addListener(
            (ListChangeListener<XYChart.Data<String, Number>>)
                c -> {
                  if (c.next()) {
                    var yAxis = (NumberAxis) chart.getYAxis();
                    var min = channel.getData().min();
                    var max = channel.getData().max();

                    yAxis.setLowerBound(min - (max - min) / 4);
                    yAxis.setUpperBound(max + (max - min) / 4);
                    yAxis.setTickUnit((max - min) / 100);
                  }
                });

    logger.debug("Chart bounded: {}", channel.getName());
  }
}
