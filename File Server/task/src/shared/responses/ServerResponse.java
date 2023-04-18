package shared.responses;

import java.io.Serial;
import java.io.Serializable;

public class ServerResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int code;
    private long id;
    private byte[] fileBytes;

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }
}
