module pl.meetingapp.frontendtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires org.json;

    opens pl.meetingapp.frontendtest to javafx.fxml;
    opens pl.meetingapp.frontendtest.controller to javafx.fxml;

    exports pl.meetingapp.frontendtest;
    exports pl.meetingapp.frontendtest.controller;
}