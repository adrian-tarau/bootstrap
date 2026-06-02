package net.microfalx.bootstrap.support.misc;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.JvmUtils;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.TimeUtils.toLocalDateTime;

@Provider
public class LibraryDataSet extends PojoDataSet<Library, PojoField<Library>, String> {

    public LibraryDataSet(DataSetFactory<Library, PojoField<Library>, String> factory, Metadata<Library, PojoField<Library>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<Library> doFindAll(Pageable pageable, Filter filterable) {
        List<Library> stores = JvmUtils.getJars().stream().map(this::from).collect(Collectors.toList());
        return getPage(stores, pageable, filterable);
    }

    private Library from(JvmUtils.Jar jar) {
        Library library = new Library();
        library.setId(jar.getId());
        library.setName(jar.getName());
        library.setVendor(defaultIfEmpty(jar.getImplementationVendor(), jar.getSpecificationVendor()));
        library.setVersion(defaultIfEmpty(jar.getImplementationVersion().toString(), jar.getSpecificationVersion()));
        library.setBuild(jar.getImplementationBuild());
        library.setBuildNumber(jar.getBuildNumber());
        library.setBuildTime(jar.getBuildTime());
        library.setFileName(jar.getFile().getName());
        library.setFileSize(jar.getFile().length());
        library.setDescription(jar.getDescription());
        library.setFileLastModified(toLocalDateTime(jar.getFile().lastModified()));
        library.setInfo(Integer.toString(jar.getAttributeCount()));
        return library;
    }
}
