package net.microfalx.bootstrap.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConvertersTest {

    @Test
    void zonedDateTime() {
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00:00.000Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00:00Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00Z", ZonedDateTime.class));
        assertEquals(ZonedDateTime.parse("2023-09-03T00:00-04:00[America/New_York]"), Converters.from("2023-09-03", ZonedDateTime.class));
    }

    @Test
    void offsetDateTime() {
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00:00.000Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00:00Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T04:00:00.000Z"), Converters.from("2023-09-03T04:00Z", OffsetDateTime.class));
        assertEquals(OffsetDateTime.parse("2023-09-03T00:00-04:00"), Converters.from("2023-09-03", OffsetDateTime.class));
    }

    @Test
    void map() {
        Map<?, ?> map = Converters.from("{}", Map.class);
        assertEquals(0, map.size());
        map = Converters.from(null, Map.class);
        assertEquals(0, map.size());
        map = Converters.from("", Map.class);
        assertEquals(0, map.size());
        map = Converters.from("{a:1, b :{c:1}}", Map.class);
        assertEquals(2, map.size());
    }

    @Test
    void collection() {
        Collection<?> collection = Converters.from("[]", Collection.class);
        assertEquals(0, collection.size());
        collection = Converters.from(null, Collection.class);
        assertEquals(0, collection.size());
        collection = Converters.from("", Collection.class);
        assertEquals(0, collection.size());
        collection = Converters.from("['a',1]", Collection.class);
        assertEquals(2, collection.size());
    }

    @Test
    void set() {
        Set<?> set = Converters.from("[]", Set.class);
        assertEquals(0, set.size());
        set = Converters.from(null, Set.class);
        assertEquals(0, set.size());
        set = Converters.from("", Set.class);
        assertEquals(0, set.size());
        set = Converters.from("['a',1, 1]", Set.class);
        assertEquals(2, set.size());
    }

    @Test
    void string() {
        assertEquals("", Converters.from("", String.class));
        assertEquals("1", Converters.from(1, String.class));
        assertEquals("1", Converters.from(1L, String.class));
        assertEquals("[ \"1\", \"2\" ]", Converters.from(new String[]{"1", "2"}, String.class));
        assertEquals("[ 1, 2 ]", Converters.from(new int[]{1, 2}, String.class));
        assertEquals("[ \"a\", 1 ]", Converters.from(Set.of(1, "a"), String.class));
        Assertions.assertThat(Converters.from(Map.of("a", 1), String.class)).isEqualToNormalizingNewlines("""
                {
                  "a" : 1
                }""");
    }

}