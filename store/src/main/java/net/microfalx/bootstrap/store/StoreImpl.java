package net.microfalx.bootstrap.store;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.AbstractIterator;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.lang.Timestampable;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.rocksdb.RocksDbManager;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.microfalx.bootstrap.store.StoreUtils.METRICS_FAILURES;
import static net.microfalx.bootstrap.store.StoreUtils.getTimer;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

final class StoreImpl<T extends Identifiable<ID>, ID> implements Store<T, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreImpl.class);

    static private final ThreadLocal<Kryo> KRYOS = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            return kryo;
        }
    };

    private final Resource resource;
    private final Store.Options options;
    private final RocksDB db;

    StoreImpl(Options options, Resource resource) {
        requireNonNull(options);
        requireNonNull(resource);
        this.options = options;
        this.resource = resource;
        this.db = RocksDbManager.getInstance().create(((FileResource) resource.toFile()).getFile());
    }

    StoreImpl(Options options, Resource resource, RocksDB db) {
        requireNonNull(options);
        requireNonNull(resource);
        requireNonNull(db);
        this.options = options;
        this.resource = resource;
        this.db = db;
    }

    public String getName() {
        return options.getName();
    }

    @Override
    public Resource getDirectory() {
        return resource;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void add(T item) {
        if (item == null) return;
        getTimer(StoreUtils.ADD_ACTION, this).record(() -> {
            byte[] data = serialize(item);
            writeContent(item.getId(), data);
        });
    }

    @Override
    public void remove(T item) {
        if (item == null) return;
        remove(item.getId());
    }

    @Override
    public void remove(ID id) {
        requireNonNull(id);
        getTimer(StoreUtils.REMOVE_ACTION, this).record(() -> {
            try {
                db.delete(ObjectUtils.toString(id).getBytes());
            } catch (Exception e) {
                throw new StoreException("Failed to remove item " + id + "'", e);
            }
        });
    }

    @Override
    public T find(ID id) {
        requireNonNull(id);
        return getTimer(StoreUtils.FIND_ACTION, this).record(() -> {
            byte[] data = readData(id);
            if (data == null) {
                return null;
            } else {
                return deserialize(data);
            }
        });
    }

    @Override
    public Collection<T> list(Query<T> query) {
        Collection<T> objects = new ArrayList<>();
        walk(query, t -> {
            objects.add(t);
            return true;
        });
        return objects;
    }

    @Override
    public void walk(Query<T> query, Function<T, Boolean> callback) {
        requireNonNull(query);
        requireNonNull(callback);
        LocalDateTime start = query.getStart();
        LocalDateTime end = query.getEnd();
        Predicate<T> filter = query.getFilter();
        getTimer(StoreUtils.WALK_ACTION, this).record(() -> {
            Iterator<T> iterator = iterator();
            while (iterator.hasNext()) {
                T object = iterator.next();
                if (start != null && end != null && !isBetween(object, start, end)) continue;
                if (filter != null && !filter.test(object)) continue;
                if (!callback.apply(object)) break;
            }
        });
    }

    @Override
    public void update(Query<T> query, Function<T, Boolean> callback) {
        walk(query, t -> {
            Boolean changed = callback.apply(t);
            if (Boolean.TRUE.equals(changed)) {
                add(t);
            } else if (Boolean.FALSE.equals(changed)) {
                return false;
            }
            return true;
        });
    }

    @Override
    public long count() {
        return RocksDbManager.getCount(db);
    }

    @Override
    public long size() {
        return RocksDbManager.getSSTSize(db);
    }

    @Override
    public long clear() {
        AtomicLong count = new AtomicLong();
        getTimer("Clear", this).record((t) -> {
            RocksIterator iterator = db.newIterator();
            iterator.seekToFirst();
            while (iterator.isValid()) {
                count.incrementAndGet();
                try {
                    db.delete(iterator.key());
                } catch (RocksDBException e) {
                    METRICS_FAILURES.count(getName());
                }
                iterator.next();
            }
        });
        return count.get();
    }

    @Override
    public void purge() {

    }

    @Override
    public Iterator<T> iterator() {
        return new IteratorImpl();
    }

    void close() {
        try {
            db.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close the ");
        }
    }

    void cleanup() {
    }

    @SuppressWarnings("unchecked")
    private T deserialize(byte[] data) {
        if (data == null) return null;
        Kryo kryo = KRYOS.get();
        ByteArrayInputStream buffer = new ByteArrayInputStream(data);
        Input input = new Input(buffer);
        return (T) kryo.readClassAndObject(input);
    }

    private byte[] serialize(T item) {
        if (item == null) return null;
        Kryo kryo = KRYOS.get();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Output output = new Output(buffer);
        kryo.writeClassAndObject(output, item);
        output.close();
        return buffer.toByteArray();
    }

    private boolean isBetween(T object, LocalDateTime start, LocalDateTime end) {
        if (!(object instanceof Timestampable)) return true;
        Timestampable<? extends Temporal> timestampable = (Timestampable<? extends Temporal>) object;
        return TimeUtils.isBetween(timestampable.getUpdatedAt(), start, end);
    }

    private byte[] readData(ID id) {
        try {
            String idAsString = ObjectUtils.toString(id);
            ReadOptions options = new ReadOptions();
            return db.get(options, idAsString.getBytes());
        } catch (RocksDBException e) {
            throw new StoreException("Failed to read item " + id + "'", e);
        }
    }

    private void writeContent(ID id, byte[] data) {
        try {
            String idAsString = ObjectUtils.toString(id);
            WriteOptions options = new WriteOptions();
            db.put(options, idAsString.getBytes(), data);
        } catch (RocksDBException e) {
            throw new StoreException("Failed to write item " + id + "'", e);
        }
    }

    private class IteratorImpl extends AbstractIterator<T> {

        private RocksIterator iterator;

        public IteratorImpl() {
            iterator = db.newIterator();
            iterator.seekToFirst();
        }

        @CheckForNull
        @Override
        protected T computeNext() {
            return getTimer("Next", StoreImpl.this).record(() -> {
                if (!iterator.isValid()) {
                    endOfData();
                    return null;
                } else {
                    T value = deserialize(iterator.value());
                    iterator.next();
                    return value;
                }
            });
        }

    }
}
