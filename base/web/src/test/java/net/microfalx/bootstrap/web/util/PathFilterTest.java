package net.microfalx.bootstrap.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PathFilterTest {

    @Mock
    private HttpServletRequest httpServletRequest;
    private String requestUri = "/";

    @BeforeEach
    void setup() {
        when(httpServletRequest.getRequestURI()).thenAnswer(invocation ->  requestUri);
    }

    @Test
    void getRootPath() {
        assertEquals("/", PathFilter.getRootPath(httpServletRequest));
        requestUri = "/a";
        assertEquals("/a", PathFilter.getRootPath(httpServletRequest));
        requestUri = "/a/b";
        assertEquals("/a", PathFilter.getRootPath(httpServletRequest));
        requestUri = "/a/b/c";
        assertEquals("/a/b", PathFilter.getRootPath(httpServletRequest));
        requestUri = "/a/b/c/d";
        assertEquals("/a/b", PathFilter.getRootPath(httpServletRequest));
    }

}