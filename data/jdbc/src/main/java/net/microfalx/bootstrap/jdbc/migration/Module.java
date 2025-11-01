package net.microfalx.bootstrap.jdbc.migration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
@ToString
public class Module implements Identifiable<String>, Nameable {

    private final String id;
    private final String name;

    int order;
    Set<String> dependsOn = new HashSet<>();
}
