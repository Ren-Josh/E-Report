package models;

import java.sql.Timestamp;

public class FollowUpRequest {
    private int furId;
    private int cdId;
    private int uiId;
    private Timestamp requestDate;
    private String status;
    private String notes;

    public FollowUpRequest() {
    }

    public int getFurId() {
        return furId;
    }

    public void setFurId(int furId) {
        this.furId = furId;
    }

    public int getCdId() {
        return cdId;
    }

    public void setCdId(int cdId) {
        this.cdId = cdId;
    }

    public int getUiId() {
        return uiId;
    }

    public void setUiId(int uiId) {
        this.uiId = uiId;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}