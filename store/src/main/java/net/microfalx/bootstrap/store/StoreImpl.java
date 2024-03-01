package net.microfalx.bootstrap.store;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.rocksdb.RocksDbManager;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public final class StoreImpl<ID, T extends Identifiable<ID>> implements Store<ID, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreImpl.class);

    private static final String CLASS_NAME_KEY = "class_name";

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

    private volatile Class<T> type;

    StoreImpl(Options options, Resource resource) {
        requireNonNull(options);
        requireNonNull(resource);
        this.options = options;
        this.resource = resource;
        this.db = RocksDbManager.getInstance().get(((FileResource) resource.toFile()).getFile());
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
        storeType(item);
        byte[] data = serialize(item);
        writeContent(item.getId(), data);
    }

    @Override
    public void remove(T item) {
        if (item == null) return;
        remove(item.getId());
    }

    @Override
    public void remove(ID id) {
        requireNonNull(id);
        try {
            db.delete(ObjectUtils.toString(id).getBytes());
        } catch (RocksDBException e) {
            throw new StoreException("Failed to remove item " + id + "'", e);
        }
    }

    @Override
    public T find(ID id) {
        requireNonNull(id);
        byte[] data = readData(id);
        if (data == null) {
            return null;
        } else {
            return deserialize(data);
        }
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public void clear() {
        // empty for now
    }

    void close() {
        try {
            db.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close the ");
        }
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

    private void storeType(T item) {
        try {
            db.put(CLASS_NAME_KEY.getBytes(), item.getClass().getName().getBytes());
        } catch (Exception e) {
            throw new StoreException("Failed to store the type for '" + getOptions().getName() + "'", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getType() {
        if (type != null) return type;
        try {
            byte[] bytes = db.get(CLASS_NAME_KEY.getBytes());
            type = (Class<T>) Class.forName(new String(bytes));
            return type;
        } catch (Exception e) {
            throw new StoreException("Failed to retrieve the type for '" + getOptions().getName() + "'", e);
        }
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
}
