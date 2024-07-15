package net.microfalx.bootstrap.core.i18n;

import com.google.common.collect.Iterators;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.CompositeResource;
import net.microfalx.resource.Resource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.*;

class I18MultiModuleMessageSource extends ResourceBundleMessageSource {

    @Override
    protected ResourceBundle doGetBundle(String basename, Locale locale) {
        ClassLoader classLoader = getBundleClassLoader();
        AggregatedResourceBundleControl control = new AggregatedResourceBundleControl();
        return ResourceBundle.getBundle(basename, locale, classLoader, control);
    }

    private InputStream getAggregatedBundleStream(String name) throws IOException {
        Resource resource = ClassPathResource.files(name);
        if (!resource.exists()) return null;
        List<InputStream> inputStreams = new ArrayList<>();
        if (resource instanceof CompositeResource) {
            for (Resource childResource : ((CompositeResource) resource).getResources()) {
                inputStreams.add(childResource.getInputStream());
            }
        } else {
            inputStreams.add(resource.getInputStream());
        }
        return new SequenceInputStream(Iterators.asEnumeration(inputStreams.iterator()));
    }

    class AggregatedResourceBundleControl extends ResourceBundle.Control {

        @Override
        public List<String> getFormats(String baseName) {
            return FORMAT_PROPERTIES;
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            InputStream inputStream = getAggregatedBundleStream(resourceName);
            return inputStream != null ? new PropertyResourceBundle(inputStream) : null;
        }
    }
}
