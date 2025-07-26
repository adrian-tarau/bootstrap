package net.microfalx.bootstrap.web.container;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebContainerUtilsTest {

    @Test
    void normalizeZoneOffset() {
        assertEquals("+08:00", WebContainerUtils.normalizeZoneOffset("+08:00"));
        assertEquals("-08:00", WebContainerUtils.normalizeZoneOffset("-08:00"));
        assertEquals("+05:00", WebContainerUtils.normalizeZoneOffset("300"));
        assertEquals("+05:00", WebContainerUtils.normalizeZoneOffset("+300"));
        assertEquals("-05:00", WebContainerUtils.normalizeZoneOffset("-300"));
    }

    @Test
    void getTimeZone() {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        assertEquals(null, WebContainerUtils.getTimeZone(httpServletRequest));
        httpServletRequest.addHeader(WebContainerUtils.TIMEZONE_HEADER, "300");
        assertEquals("+05:00", WebContainerUtils.getTimeZone(httpServletRequest).toString());

        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader(WebContainerUtils.TIMEZONE_HEADER, "-300");
        assertEquals("-05:00", WebContainerUtils.getTimeZone(httpServletRequest).toString());
    }

}