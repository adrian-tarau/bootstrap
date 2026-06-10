package net.microfalx.bootstrap.cli.util;

import net.microfalx.lang.JvmUtils;

public class TestTool extends Tool<TestTool> {

    public TestTool() {
        super("test", "Test");
    }

    @Override
    protected String getExecutable() {
        return JvmUtils.isWindows() ? "rundll32.exe" : "true";
    }

    @Override
    protected String[] getFiles() {
        return new String[0];
    }
}
