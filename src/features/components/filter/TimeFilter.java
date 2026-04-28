package features.components.filter;

import java.util.Calendar;
import java.util.Date;

/**
 * TimeFilter
 * 
 * A backend-accessible data model representing structured time-based filtering.
 */
public class TimeFilter {

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum FilterType {
        ALL_TIME,
        SPAN_OF_YEARS,
        SINGLE_YEAR,
        SPAN_OF_MONTHS,
        SINGLE_MONTH,
        SINGLE_WEEK
    }

    // ========================================================================
    // FIELDS
    // ========================================================================

    private final FilterType filterType;
    private final int startYear;
    private final int endYear;
    private final int year;
    private final int startMonth;
    private final int endMonth;
    private final int month;
    private final int weekOfMonth;

    // ========================================================================
    // CONSTRUCTORS (Private - use factory methods)
    // ========================================================================

    private TimeFilter(FilterType filterType, int startYear, int endYear,
            int year, int startMonth, int endMonth, int month, int weekOfMonth) {
        this.filterType = filterType;
        this.startYear = startYear;
        this.endYear = endYear;
        this.year = year;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.month = month;
        this.weekOfMonth = weekOfMonth;
    }

    // ========================================================================
    // FACTORY METHODS
    // ========================================================================

    /** Creates an "All Time" filter — start/end dates are null (no restriction). */
    public static TimeFilter forAllTime() {
        return new TimeFilter(FilterType.ALL_TIME, 0, 0, 0, 0, 0, 0, 0);
    }

    public static TimeFilter forYearSpan(int startYear, int endYear) {
        return new TimeFilter(FilterType.SPAN_OF_YEARS, startYear, endYear,
                startYear, 0, 0, 0, 0);
    }

    public static TimeFilter forSingleYear(int year) {
        int effectiveYear = (year <= 0) ? getCurrentYear() : year;
        return new TimeFilter(FilterType.SINGLE_YEAR, 0, 0,
                effectiveYear, 0, 0, 0, 0);
    }

    public static TimeFilter forMonthSpan(int year, int startMonth, int endMonth) {
        int effectiveYear = (year <= 0) ? getCurrentYear() : year;
        return new TimeFilter(FilterType.SPAN_OF_MONTHS, 0, 0,
                effectiveYear, startMonth, endMonth, 0, 0);
    }

    public static TimeFilter forSingleMonth(int year, int month) {
        int effectiveYear = (year <= 0) ? getCurrentYear() : year;
        return new TimeFilter(FilterType.SINGLE_MONTH, 0, 0,
                effectiveYear, 0, 0, month, 0);
    }

    public static TimeFilter forSingleWeek(int year, int month, int weekOfMonth) {
        int effectiveYear = (year <= 0) ? getCurrentYear() : year;
        return new TimeFilter(FilterType.SINGLE_WEEK, 0, 0,
                effectiveYear, 0, 0, month, weekOfMonth);
    }

    // ========================================================================
    // DATE COMPUTATION METHODS (Backend API)
    // ========================================================================

    public Date getStartDate() {
        if (filterType == FilterType.ALL_TIME) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.clear();

        switch (filterType) {
            case SPAN_OF_YEARS:
                cal.set(startYear, Calendar.JANUARY, 1, 0, 0, 0);
                break;

            case SINGLE_YEAR:
                cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
                break;

            case SPAN_OF_MONTHS:
                cal.set(year, startMonth, 1, 0, 0, 0);
                break;

            case SINGLE_MONTH:
                cal.set(year, month, 1, 0, 0, 0);
                break;

            case SINGLE_WEEK:
                cal.set(year, month, 1, 0, 0, 0);
                cal.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                break;

            default:
                return null;
        }

        return cal.getTime();
    }

    public Date getEndDate() {
        if (filterType == FilterType.ALL_TIME) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.clear();

        switch (filterType) {
            case SPAN_OF_YEARS:
                cal.set(endYear, Calendar.DECEMBER, 31, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                break;

            case SINGLE_YEAR:
                cal.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                break;

            case SPAN_OF_MONTHS:
                cal.set(year, endMonth, 1, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                cal.add(Calendar.MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case SINGLE_MONTH:
                cal.set(year, month, 1, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                cal.add(Calendar.MONTH, 1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case SINGLE_WEEK:
                cal.set(year, month, 1, 23, 59, 59);
                cal.set(Calendar.MILLISECOND, 999);
                cal.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
                int firstDay = cal.getFirstDayOfWeek();
                cal.set(Calendar.DAY_OF_WEEK, firstDay);
                cal.add(Calendar.DAY_OF_WEEK, 6);
                if (cal.get(Calendar.MONTH) != month) {
                    cal.set(year, month, 1);
                    cal.add(Calendar.MONTH, 1);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                }
                break;

            default:
                return null;
        }

        return cal.getTime();
    }

    public String getSqlStartDateString() {
        Date d = getStartDate();
        return d != null ? formatForSql(d) : null;
    }

    public String getSqlEndDateString() {
        Date d = getEndDate();
        return d != null ? formatForSql(d) : null;
    }

    public String getDescription() {
        String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

        switch (filterType) {
            case ALL_TIME:
                return "All Time";
            case SPAN_OF_YEARS:
                return startYear + " - " + endYear;
            case SINGLE_YEAR:
                return String.valueOf(year);
            case SPAN_OF_MONTHS:
                return months[startMonth] + " - " + months[endMonth] + " " + year;
            case SINGLE_MONTH:
                return months[month] + " " + year;
            case SINGLE_WEEK:
                return "Week " + weekOfMonth + " of " + months[month] + " " + year;
            default:
                return "Unknown";
        }
    }

    // ========================================================================
    // GETTERS
    // ========================================================================

    public FilterType getFilterType() {
        return filterType;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public int getYear() {
        return year;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public int getMonth() {
        return month;
    }

    public int getWeekOfMonth() {
        return weekOfMonth;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private String formatForSql(Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public String toString() {
        return "TimeFilter{" +
                "type=" + filterType +
                ", description='" + getDescription() + "'" +
                ", start=" + getSqlStartDateString() +
                ", end=" + getSqlEndDateString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TimeFilter that = (TimeFilter) o;
        return filterType == that.filterType &&
                startYear == that.startYear &&
                endYear == that.endYear &&
                year == that.year &&
                startMonth == that.startMonth &&
                endMonth == that.endMonth &&
                month == that.month &&
                weekOfMonth == that.weekOfMonth;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(filterType, startYear, endYear, year,
                startMonth, endMonth, month, weekOfMonth);
    }
}