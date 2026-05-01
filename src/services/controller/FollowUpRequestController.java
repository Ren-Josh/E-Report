package services.controller;

import daos.FollowUpRequestDao;
import models.FollowUpRequest;

/**
 * Controller for follow-up request operations.
 */
public class FollowUpRequestController {

    private final FollowUpRequestDao dao;

    public FollowUpRequestController() {
        this.dao = new FollowUpRequestDao();
    }

    public boolean requestFollowUp(int cdId, int uiId, String notes) {
        FollowUpRequest req = new FollowUpRequest();
        req.setCdId(cdId);
        req.setUiId(uiId);
        req.setStatus("Pending");
        req.setNotes(notes != null && !notes.isBlank() ? notes : null);
        return dao.insert(req);
    }

    public boolean hasActiveFollowUp(int cdId) {
        return dao.hasActiveRequest(cdId);
    }

    public FollowUpRequest getLatestFollowUp(int cdId) {
        return dao.findLatestByComplaintId(cdId);
    }

    public boolean acknowledgeFollowUp(int furId) {
        return dao.updateStatus(furId, "Acknowledged");
    }

    public boolean resolveFollowUp(int furId) {
        return dao.updateStatus(furId, "Resolved");
    }
}