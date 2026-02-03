package net.microfalx.bootstrap.web.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssetTest {

    @Test
    void getUri() {
        Asset asset = Asset.uri(Asset.Type.JAVA_SCRIPT, "http://example.com/script.js")
                .parameter("a", 1).build();
        assertEquals("http://example.com/script.js", asset.getUri().toASCIIString());
        assertEquals(1, asset.getParameters().size());
    }

    @Test
    void toExternalUri() {
        Asset asset = Asset.uri(Asset.Type.JAVA_SCRIPT, "http://example.com/script.js")
                .parameter("a", 1).async(true).defer(true).build();
        assertEquals("http://example.com/script.js?a=1", asset.toExternalUri().toASCIIString());
    }
}