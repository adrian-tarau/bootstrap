package net.microfalx.bootstrap.web.controller;

import jakarta.annotation.security.RolesAllowed;
import net.microfalx.bootstrap.web.application.annotation.SystemTheme;
import net.microfalx.lang.annotation.Module;

@SystemTheme
@RolesAllowed("admin")
@Module("System")
public abstract class SystemPageController extends PageController{
}
