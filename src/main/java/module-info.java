module org.blab.drum {
  requires org.blab.vcas;

  requires javafx.controls;
  requires javafx.fxml;

  requires org.apache.logging.log4j;
  requires org.apache.logging.log4j.core;
  requires javafx.base;
  requires jdk.compiler;

  opens org.blab.drum to javafx.fxml;
  exports org.blab.drum;
}
