package models;

import java.sql.Timestamp;

public class ComplaintAction {
    private String CD_ID, actionTaken, recommendation, oic;
    private Timestamp resolutionDateTime, dateTimeAssigned;

    public ComplaintAction(String CD_ID, String actionTaken, String recommendation, String oic,
            Timestamp dateTimeAssigned, Timestamp resolutionDateTime) {
        this.CD_ID = CD_ID;
        this.actionTaken = actionTaken;
        this.recommendation = recommendation;
        this.oic = oic;
        this.dateTimeAssigned = dateTimeAssigned;
        this.resolutionDateTime = resolutionDateTime;
    }

    public ComplaintAction() {
    };

    public String getCD_ID() {
        return CD_ID;
    }

    public void setCD_ID(String CD_ID) {
        this.CD_ID = CD_ID;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getOIC() {
        return oic;
    }

    public void setOIC(String oic) {
        this.oic = oic;
    }

    public Timestamp getDateTimeAssigned() {
        return dateTimeAssigned;
    }

    public void setDateTimeAssigned(Timestamp dateTimeAssigned) {
        this.dateTimeAssigned = dateTimeAssigned;
    }

    public Timestamp getResolutionDateTime() {
        return resolutionDateTime;
    }

    public void setResolutionDateTime(Timestamp resolutionDateTime) {
        this.resolutionDateTime = resolutionDateTime;
    }
}