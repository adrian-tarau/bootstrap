package net.microfalx.bootstrap.core.utils;

import org.junit.jupiter.api.Test;

import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IpWhoIsTest {

    @Test
    void resolveGoogle() {
        IpWhoIs ip = IpWhoIs.lookup("173.63.203.95");
        assertNotNull(ip.getCountry());
        assertNotNull(ip.getCountryCode());
        assertNotNull(ip.getRegion());
        assertNotNull(ip.getRegionCode());
        assertNotNull(ip.getCity());
        assertNotNull(ip.getLatitude());
        assertNotNull(ip.getLongitude());
        assertNotNull(ip.getDescription());
    }

    @Test
    void resolveInternal() {
        IpWhoIs ip = IpWhoIs.lookup("192.168.1.252");
        assertEquals(EMPTY_STRING, ip.getCountry());
        assertEquals(EMPTY_STRING, ip.getCountryCode());
        assertEquals(EMPTY_STRING, ip.getRegion());
        assertEquals(EMPTY_STRING, ip.getRegionCode());
        assertEquals(EMPTY_STRING, ip.getCity());
        assertEquals(EMPTY_STRING, ip.getDescription());
    }


}