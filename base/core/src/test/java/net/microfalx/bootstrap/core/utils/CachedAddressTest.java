package net.microfalx.bootstrap.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CachedAddressTest {

    @Test
    void getAnyAddress() {
        assertEquals("0.0.0.0", CachedAddress.getAnyAddress().getHostAddress());
        assertEquals("0.0.0.0", CachedAddress.getAnyAddress().getHostName());
    }

    @Test
    void isIP() {
        assertTrue(CachedAddress.isIP("127.0.0.1"));
        assertFalse(CachedAddress.isIP("google.com"));
    }

    @Test
    void isLocalHost() {
        assertTrue(CachedAddress.isLocalHost("127.0.0.1"));
        assertFalse(CachedAddress.isLocalHost("google.com"));
    }

    @Test
    void getDomainName() {
        assertEquals("127.0.0.1", CachedAddress.getDomainName("127.0.0.1"));
        assertEquals("10.0.0.1", CachedAddress.getDomainName("10.0.0.1"));
        assertEquals("google.com", CachedAddress.getDomainName("maps.google.com"));
    }

    @Test
    void getCanonicalHostName() {
        CachedAddress address = CachedAddress.get("google.com");
        assertNotNull(address.getCanonicalHostName());
    }

    @Test
    void getGoogle() {
        CachedAddress address = CachedAddress.get("google.com");
        assertFalse(address.isIp());
        assertTrue(address.isResolved());
        assertNotNull(address.getAddress());
        assertFalse(address.getAddresses().isEmpty());
        assertNotNull(address.getHostname());
        assertNotNull(address.getCanonicalHostName());
        assertFalse(address.getAliases().isEmpty());
    }

    @Test
    void getLocalhost() {
        CachedAddress address = CachedAddress.get("localhost");
        assertFalse(address.isIp());
        assertTrue(address.isResolved());
        assertNotNull(address.getAddress());
        assertFalse(address.getAddresses().isEmpty());
        assertNotNull(address.getHostname());
        assertNotNull(address.getCanonicalHostName());
        assertFalse(address.getAliases().isEmpty());
    }
}