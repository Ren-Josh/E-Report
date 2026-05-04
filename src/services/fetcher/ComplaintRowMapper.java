package services.fetcher;

import models.ComplaintDetail;

/**
 * Centralized complaint-to-table-row conversion.
 * Eliminates duplicate row-building logic across all fetchers.
 */
public final class ComplaintRowMapper {

    /** Private constructor prevents instantiation; this is a utility class. */
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
        // Guard against null input to prevent NullPointerException in callers.
        if (cd == null)
            return new Object[COLUMN_COUNT];

        Object[] row = new Object[COLUMN_COUNT];
        row[0] = cd.getComplaintId();
        row[1] = nullSafe(cd.getType());
        row[2] = nullSafe(cd.getPurok());
        row[3] = cd.getDateTime();
        // Fallback to creation time if no update timestamp exists.
        row[4] = cd.getLastUpdateTimestamp() != null
                ? cd.getLastUpdateTimestamp()
                : cd.getDateTime();
        row[5] = nullSafe(cd.getCurrentStatus());
        // Static action label used by JTable button renderers/editors.
        row[6] = "View";
        return row;
    }

    /**
     * Returns the input string unchanged, or an empty string if the input is null.
     * Prevents null values from appearing in table cells where a String is
     * expected.
     * 
     * @param s the string to sanitize
     * @return the original string, or "" if null
     */
    private static String nullSafe(String s) {
        return s != null ? s : "";
    }
}