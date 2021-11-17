module com.udacity.catpoint.security {
    requires com.miglayout.swing;
    requires java.desktop;
    requires com.google.gson;
    requires com.google.common;
    requires java.prefs;
    requires com.udacity.catpoint.image;
    opens com.udacity.catpoint.security.data to com.google.gson;
}
