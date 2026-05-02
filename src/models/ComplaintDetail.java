package models;

import java.sql.Timestamp;

public class ComplaintDetail {
    private String currentStatus, subject, type, purok, personsInvolved, details;
    private byte[] photoAttachmentBytes;
    private Timestamp dateTime, lastUpdateTimestamp;
    private double longitude, latitude;
    private int CD_ID;
    private String photoName;
    private String photoType;
    private String street;
    private Integer photoSize;

    public ComplaintDetail(int CD_ID, String currentStatus, String subject, String type, Timestamp dateTime,
            double longitude, double latitude, String purok, String personsInvolved, String details,
            byte[] photoAttachmentBytes) {
        this.CD_ID = CD_ID;
        this.currentStatus = currentStatus;
        this.subject = subject;
        this.type = type;
        this.dateTime = dateTime;
        this.longitude = longitude;
        this.latitude = latitude;
        this.purok = purok;
        this.personsInvolved = personsInvolved;
        this.details = details;
        this.photoAttachmentBytes = photoAttachmentBytes;
    }

    public ComplaintDetail() {
    };

    public int getComplaintId() {
        return CD_ID;
    }

    public void setComplaintId(int CD_ID) {
        this.CD_ID = CD_ID;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPurok() {
        return purok;
    }

    public void setPurok(String purok) {
        this.purok = purok;
    }

    public String getPersonsInvolved() {
        return personsInvolved;
    }

    public void setPersonsInvolved(String personsInvolved) {
        this.personsInvolved = personsInvolved;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public byte[] getPhotoAttachmentBytes() {
        return photoAttachmentBytes;
    }

    public void setPhotoAttachmentBytes(byte[] photoAttachmentBytes) {
        this.photoAttachmentBytes = photoAttachmentBytes;
    }

    public void setLastUpdateTimestamp(Timestamp timeStamp) {
        this.lastUpdateTimestamp = timeStamp;
    }

    public Timestamp getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getPhotoType() {
        return photoType;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    public Integer getPhotoSize() {
        return photoSize;
    }

    public void setPhotoSize(Integer photoSize) {
        this.photoSize = photoSize;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}