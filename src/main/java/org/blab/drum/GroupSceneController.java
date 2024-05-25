package org.blab.drum;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.blab.drum.model.Channel;
import org.blab.drum.model.DrumService;

public class GroupSceneController implements SceneController {
  @FXML TabPane tabPane;

  private final String groupName;

  public GroupSceneController(String groupName) {
    this.groupName = groupName;
  }

  public void bindDrumService(DrumService drumService) {
    var group = drumService.getGroups().get(groupName);
    group.getChannels().forEach((n, c) -> tabPane.getTabs().add(createTabForChannel(c)));
  }

  private Tab createTabForChannel(Channel channel) {
    return new Tab(channel.getName(), wrapChart(createChartForChannel(channel)));
  }

  private AnchorPane wrapChart(LineChart<String, Number> chart) {
    var pane = new AnchorPane(chart);

    AnchorPane.setTopAnchor(chart, 0.0);
    AnchorPane.setLeftAnchor(chart, 0.0);
    AnchorPane.setRightAnchor(chart, 0.0);
    AnchorPane.setBottomAnchor(chart, 0.0);

    return pane;
  }

  private LineChart<String, Number> createChartForChannel(Channel channel) {
    final CategoryAxis xAxis = new CategoryAxis();
    xAxis.setSide(Side.BOTTOM);
    xAxis.setAnimated(false);
    xAxis.setLabel("Time");

    final NumberAxis yAxis = new NumberAxis();
    yAxis.setSide(Side.LEFT);
    yAxis.setAnimated(false);
    yAxis.setAutoRanging(false);
    yAxis.setLabel("Value");

    final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
    chart.setLegendVisible(false);
    chart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);

    bindChart(chart, channel);
    return chart;
  }

  private void bindChart(LineChart<String, Number> chart, Channel channel) {
    XYChart.Series<String, Number> series = new XYChart.Series<>();

    series.setData(channel.getData().getShadow());
    series
        .getData()
        .addListener(
            (ListChangeListener<XYChart.Data<String, Number>>)
                c -> {
                  if (c.next()) {
                    var yAxis = (NumberAxis) chart.getYAxis();
                    var min = channel.getData().min();
                    var max = channel.getData().max();

                    yAxis.setLowerBound(min - (max - min) / 4);
                    yAxis.setUpperBound(max + (max - min) / 4);
                    yAxis.setTickUnit((max - min) / 10);

                    c.getList()
                        .forEach(
                            node -> {
                              var tooltip =
                                  new Tooltip(
                                      String.format(
                                          "%f\n%s",
                                          node.getYValue().doubleValue(), node.getXValue()));
                              tooltip.setShowDelay(Duration.ZERO);
                              Tooltip.install(node.getNode(), tooltip);
                            });
                  }
                });

    chart.getData().add(series);
  }
}
