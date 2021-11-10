module com.udacity.catpoint.image {
    requires slf4j.api;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.regions;
    requires java.desktop;
    exports com.udacity.catpoint.image.service;
}
