package net.microfalx.bootstrap.ai.core.repository;

import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.ModelRepository;
import net.microfalx.lang.annotation.Order;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

import java.io.IOException;

@Provider
@Order(Order.AFTER)
public class InternetModelRepository implements ModelRepository {

    @Override
    public boolean supports(Model model) {
        return model.getDownloadUri() != null;
    }

    @Override
    public Resource resolve(Model model) throws IOException {
        Resource url = Resource.url(model.getDownloadUri().toURL());
        return url.exists() ? url : null;
    }
}
