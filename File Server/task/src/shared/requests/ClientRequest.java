package shared.requests;

import java.io.Serial;
import java.io.Serializable;

public class ClientRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String method;
    private String nameOrId;
    private String requestBy;
    private byte[] fileContent;

    public ClientRequest(String method) {
        this.method = method;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getNameOrId() {
        return nameOrId;
    }
    public void setNameOrId(String nameOrId) {
        this.nameOrId = nameOrId;
    }
    public String getRequestBy() {
        return requestBy;
    }
    public void setRequestBy(String requestBy) {
        this.requestBy = requestBy;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }
}
