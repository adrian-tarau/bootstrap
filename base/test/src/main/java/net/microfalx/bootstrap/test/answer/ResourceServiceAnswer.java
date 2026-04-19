package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.resource.ResourceLocation;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.test.annotation.AnswerFor;
import net.microfalx.resource.Resource;
import org.mockito.invocation.InvocationOnMock;

import java.io.File;

import static net.microfalx.lang.FileUtils.validateDirectoryExists;

@SuppressWarnings("unused")
@AnswerFor(ResourceService.class)
public class ResourceServiceAnswer extends AbstractAnswer {

    private File workspaceDirectory;

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        return switch (methodName) {
            case "getPersisted" -> {
                String name = invocation.getArgument(0);
                yield getResource(ResourceLocation.PERSISTED, name);
            }
            case "getTransient" -> {
                String name = invocation.getArgument(0);
                yield getResource(ResourceLocation.TRANSIENT, name);
            }
            case "getShared" -> {
                String name = invocation.getArgument(0);
                yield getResource(ResourceLocation.SHARED, name);
            }
            case "get" -> {
                ResourceLocation location = invocation.getArgument(0);
                String name = invocation.getArgument(1);
                yield getResource(location, name);
            }
            default -> super.answer(invocation);
        };
    }

    private Resource getResource(ResourceLocation location, String name) {
        return getResource(location).resolve(name, Resource.Type.DIRECTORY);
    }

    private Resource getResource(ResourceLocation location) {
        File directory = validateDirectoryExists(new File(getWorkspaceDirectory(), location.name().toLowerCase()));
        return Resource.directory(directory);
    }

    public File getWorkspaceDirectory() {
        if (workspaceDirectory == null) {
            workspaceDirectory = validateDirectoryExists(new File(getContext().getWorkingDirectory(), "resource"));
        }
        return workspaceDirectory;
    }
}
