module org.blab.drum {
  requires org.blab.vcas;
  requires javafx.controls;
  requires javafx.fxml;
  requires org.apache.logging.log4j;

  opens org.blab.drum to javafx.fxml;
  exports org.blab.drum;
}
