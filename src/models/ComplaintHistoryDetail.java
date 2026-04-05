package models;

import java.sql.Timestamp;

public class ComplaintHistoryDetail {
    private String status, process, updatedBy;
    private Timestamp dateTimeUpdated;

    public ComplaintHistoryDetail(String status, String process, Timestamp dateTimeUpdated, String updatedBy) {
        this.status = status;
        this.process = process;
        this.dateTimeUpdated = dateTimeUpdated;
        this.updatedBy = updatedBy;
    }

    public ComplaintHistoryDetail() {
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public Timestamp getDateTimeUpdated() {
        return dateTimeUpdated;
    }

    public void setDateTimeUpdated(Timestamp dateTimeUpdated) {
        this.dateTimeUpdated = dateTimeUpdated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
