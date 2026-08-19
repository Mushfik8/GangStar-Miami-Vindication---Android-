package javax.microedition.rms;

public interface RecordEnumeration {
    int numRecords();
    byte[] nextRecord() throws RecordStoreException;
    int nextRecordId() throws RecordStoreException;
    byte[] previousRecord() throws RecordStoreException;
    int previousRecordId() throws RecordStoreException;
    boolean hasNextElement();
    boolean hasPreviousElement();
    void reset();
    void rebuild();
    void keepUpdated(boolean keepUpdated);
    boolean isKeptUpdated();
    void destroy();
}
