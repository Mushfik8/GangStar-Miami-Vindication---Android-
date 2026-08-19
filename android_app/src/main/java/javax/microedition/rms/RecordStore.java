package javax.microedition.rms;

import android.content.Context;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordStore {
    private static File storageDir;
    private static final Map<String, RecordStore> openStores = new HashMap<>();

    private final String name;
    private final File storeFile;
    private final Map<Integer, byte[]> records = new HashMap<>();
    private int nextRecordId = 1;
    private boolean isOpen = true;

    public static void init(Context context) {
        if (context != null) {
            storageDir = new File(context.getFilesDir(), "rms");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
        }
    }

    public static synchronized RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws RecordStoreException {
        if (recordStoreName == null || recordStoreName.isEmpty() || recordStoreName.length() > 32) {
            throw new IllegalArgumentException("Invalid RecordStore name");
        }

        if (openStores.containsKey(recordStoreName)) {
            return openStores.get(recordStoreName);
        }

        if (storageDir == null) {
            storageDir = new File(System.getProperty("java.io.tmpdir"), "rms");
            storageDir.mkdirs();
        }

        File file = new File(storageDir, recordStoreName + ".rms");
        if (!file.exists() && !createIfNecessary) {
            throw new RecordStoreNotFoundException("RecordStore not found: " + recordStoreName);
        }

        RecordStore rs = new RecordStore(recordStoreName, file);
        openStores.put(recordStoreName, rs);
        return rs;
    }

    public static synchronized void deleteRecordStore(String recordStoreName) throws RecordStoreException {
        if (openStores.containsKey(recordStoreName)) {
            throw new RecordStoreException("RecordStore is currently open");
        }
        if (storageDir != null) {
            File file = new File(storageDir, recordStoreName + ".rms");
            if (file.exists()) {
                file.delete();
            } else {
                throw new RecordStoreNotFoundException("RecordStore not found: " + recordStoreName);
            }
        }
    }

    private static class RmsFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name != null && name.endsWith(".rms");
        }
    }

    public static synchronized String[] listRecordStores() {
        if (storageDir == null || !storageDir.exists()) return null;
        File[] files = storageDir.listFiles(new RmsFilenameFilter());
        if (files == null || files.length == 0) return null;
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String n = files[i].getName();
            names[i] = n.substring(0, n.length() - 4);
        }
        return names;
    }

    private RecordStore(String name, File file) throws RecordStoreException {
        this.name = name;
        this.storeFile = file;
        load();
    }

    private void checkOpen() throws RecordStoreException {
        if (!isOpen) throw new RecordStoreException("RecordStore is closed");
    }

    private synchronized void load() throws RecordStoreException {
        if (storeFile != null && storeFile.exists() && storeFile.length() > 0) {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(storeFile))) {
                nextRecordId = dis.readInt();
                int count = dis.readInt();
                records.clear();
                for (int i = 0; i < count; i++) {
                    int id = dis.readInt();
                    int len = dis.readInt();
                    byte[] data = new byte[len];
                    dis.readFully(data);
                    records.put(id, data);
                }
            } catch (IOException e) {
                throw new RecordStoreException("Failed to read RecordStore: " + e.getMessage());
            }
        }
    }

    private synchronized void save() throws RecordStoreException {
        if (storeFile != null) {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(storeFile))) {
                dos.writeInt(nextRecordId);
                dos.writeInt(records.size());
                for (Map.Entry<Integer, byte[]> entry : records.entrySet()) {
                    dos.writeInt(entry.getKey());
                    byte[] val = entry.getValue();
                    dos.writeInt(val.length);
                    dos.write(val);
                }
                dos.flush();
            } catch (IOException e) {
                throw new RecordStoreException("Failed to save RecordStore: " + e.getMessage());
            }
        }
    }

    public synchronized void closeRecordStore() throws RecordStoreException {
        checkOpen();
        save();
        isOpen = false;
        openStores.remove(name);
    }

    public synchronized int getNumRecords() throws RecordStoreException {
        checkOpen();
        return records.size();
    }

    public synchronized int getSize() throws RecordStoreException {
        checkOpen();
        int total = 0;
        for (byte[] r : records.values()) {
            total += r.length;
        }
        return total;
    }

    public int getSizeAvailable() throws RecordStoreException {
        checkOpen();
        return 1024 * 1024; // 1 MB available
    }

    public synchronized int getNextRecordID() throws RecordStoreException {
        checkOpen();
        return nextRecordId;
    }

    public synchronized int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreException {
        checkOpen();
        int id = nextRecordId++;
        byte[] copy = new byte[numBytes];
        if (data != null && numBytes > 0) {
            System.arraycopy(data, offset, copy, 0, numBytes);
        }
        records.put(id, copy);
        save();
        return id;
    }

    public synchronized void setRecord(int recordId, byte[] newData, int offset, int numBytes) throws RecordStoreException {
        checkOpen();
        if (!records.containsKey(recordId)) {
            throw new RecordStoreException("Invalid record ID: " + recordId);
        }
        byte[] copy = new byte[numBytes];
        if (newData != null && numBytes > 0) {
            System.arraycopy(newData, offset, copy, 0, numBytes);
        }
        records.put(recordId, copy);
        save();
    }

    public synchronized byte[] getRecord(int recordId) throws RecordStoreException {
        checkOpen();
        byte[] rec = records.get(recordId);
        if (rec == null) {
            throw new RecordStoreException("Record not found: " + recordId);
        }
        byte[] copy = new byte[rec.length];
        System.arraycopy(rec, 0, copy, 0, rec.length);
        return copy;
    }

    public synchronized int getRecord(int recordId, byte[] buffer, int offset) throws RecordStoreException {
        checkOpen();
        byte[] rec = records.get(recordId);
        if (rec == null) {
            throw new RecordStoreException("Record not found: " + recordId);
        }
        System.arraycopy(rec, 0, buffer, offset, rec.length);
        return rec.length;
    }

    public synchronized void deleteRecord(int recordId) throws RecordStoreException {
        checkOpen();
        if (!records.containsKey(recordId)) {
            throw new RecordStoreException("Record not found: " + recordId);
        }
        records.remove(recordId);
        save();
    }

    public synchronized RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated) throws RecordStoreException {
        checkOpen();
        List<Integer> ids = new ArrayList<>(records.keySet());
        Collections.sort(ids);
        return new SimpleRecordEnumeration(this, ids);
    }

    private static class SimpleRecordEnumeration implements RecordEnumeration {
        private final RecordStore store;
        private final List<Integer> idList;
        private int index = 0;

        SimpleRecordEnumeration(RecordStore store, List<Integer> ids) {
            this.store = store;
            this.idList = ids;
        }

        @Override
        public int numRecords() {
            return idList.size();
        }

        @Override
        public byte[] nextRecord() throws RecordStoreException {
            if (!hasNextElement()) throw new RecordStoreException("No more records");
            int id = idList.get(index++);
            return store.getRecord(id);
        }

        @Override
        public int nextRecordId() throws RecordStoreException {
            if (!hasNextElement()) throw new RecordStoreException("No more records");
            return idList.get(index++);
        }

        @Override
        public byte[] previousRecord() throws RecordStoreException {
            if (!hasPreviousElement()) throw new RecordStoreException("No previous records");
            int id = idList.get(--index);
            return store.getRecord(id);
        }

        @Override
        public int previousRecordId() throws RecordStoreException {
            if (!hasPreviousElement()) throw new RecordStoreException("No previous records");
            return idList.get(--index);
        }

        @Override
        public boolean hasNextElement() {
            return index < idList.size();
        }

        @Override
        public boolean hasPreviousElement() {
            return index > 0;
        }

        @Override
        public void reset() {
            index = 0;
        }

        @Override
        public void rebuild() {}
        @Override
        public void keepUpdated(boolean keepUpdated) {}
        @Override
        public boolean isKeptUpdated() { return false; }
        @Override
        public void destroy() {}
    }
}
