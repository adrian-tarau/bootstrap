package net.microfalx.bootstrap.web.dataset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An implementation of a {@link Page} using a {@link List}.
 *
 * @param <M> the model type
 */
public class DataSetPage<M> implements Page<M> {

    private final Pageable pageable;
    private final List<M> models;

    public DataSetPage(Pageable pageable, List<M> models) {
        requireNonNull(pageable);
        requireNonNull(models);
        this.pageable = pageable;
        this.models = new ArrayList<>(models);
    }

    @Override
    public int getTotalPages() {
        return 1 + models.size() / pageable.getPageSize();
    }

    @Override
    public long getTotalElements() {
        return models.size();
    }

    @Override
    public <U> Page<U> map(Function<? super M, ? extends U> converter) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public int getNumber() {
        return pageable.getPageNumber();
    }

    @Override
    public int getSize() {
        return pageable.getPageSize();
    }

    @Override
    public int getNumberOfElements() {
        return getEndIndex() - getStartIndex();
    }

    @Override
    public List<M> getContent() {
        if (models.isEmpty()) return emptyList();
        return models.subList(getStartIndex(), getEndIndex());
    }

    @Override
    public boolean hasContent() {
        return getNumberOfElements() > 0;
    }

    @Override
    public Sort getSort() {
        return pageable.getSort();
    }

    @Override
    public boolean isFirst() {
        return pageable.getPageNumber() == 0;
    }

    @Override
    public boolean isLast() {
        return pageable.getPageNumber() == (getTotalPages() - 1);
    }

    @Override
    public boolean hasNext() {
        return pageable.getPageNumber() < (getTotalPages() - 1);
    }

    @Override
    public boolean hasPrevious() {
        return pageable.getPageNumber() > 0;
    }

    @Override
    public Pageable nextPageable() {
        if (models.isEmpty()) return Pageable.ofSize(pageable.getPageSize());
        return PageRequest.of(Math.min(getTotalPages() - 1, pageable.getPageNumber() + 1), pageable.getPageSize(), pageable.getSort());
    }

    @Override
    public Pageable previousPageable() {
        if (models.isEmpty()) return Pageable.ofSize(pageable.getPageSize());
        return PageRequest.of(Math.max(pageable.getPageNumber() - 1, 0), pageable.getPageSize(), pageable.getSort());
    }

    @Override
    public Iterator<M> iterator() {
        return getContent().iterator();
    }

    @Override
    public String toString() {
        return "DataSetPage{" +
                "pageable=" + pageable +
                ", models=" + models.size() +
                '}';
    }

    private int getStartIndex() {
        return pageable.getPageNumber() * pageable.getPageSize();
    }

    private int getEndIndex() {
        return Math.min(getStartIndex() + pageable.getPageSize(), models.size());
    }
}
