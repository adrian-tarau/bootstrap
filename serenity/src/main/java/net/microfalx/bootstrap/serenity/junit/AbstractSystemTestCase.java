package net.microfalx.bootstrap.serenity.junit;

import net.microfalx.bootstrap.serenity.annotation.BootstrapSystemTest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.annotations.CastMember;

@BootstrapSystemTest
public abstract class AbstractSystemTestCase {

    @CastMember(name = "QA")
    protected Actor actor;
}
