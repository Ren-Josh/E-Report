package services.fetcher;

import models.ComplaintDetail;

/**
 * Centralized complaint-to-table-row conversion.
 * Eliminates duplicate row-building logic across all fetchers.
 */
public final class ComplaintRowMapper {

    private ComplaintRowMapper() {
    } // utility class

    /**
     * Standard dashboard table columns: ID, Type, Purok, Submitted, Updated,
     * Status, Action
     */
    public static final int COLUMN_COUNT = 7;

    /**
     * Converts a ComplaintDetail into the standard Object[] row format.
     *
     * @param cd the complaint detail
     * @return Object[7] = {id, type, purok, dateSubmitted, lastUpdate, status,
     *         "View"}
     */
    public static Object[] toRow(ComplaintDetail cd) {
        if (cd == null)
            return new Object[COLUMN_COUNT];

        Object[] row = new Object[COLUMN_COUNT];
        row[0] = cd.getComplaintId();
        row[1] = nullSafe(cd.getType());
        row[2] = nullSafe(cd.getPurok());
        row[3] = cd.getDateTime();
        row[4] = cd.getLastUpdateTimestamp() != null
                ? cd.getLastUpdateTimestamp()
                : cd.getDateTime();
        row[5] = nullSafe(cd.getCurrentStatus());
        row[6] = "View";
        return row;
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }
}