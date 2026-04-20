package features.core.dashboardpanel.captain;

public class ActivityItem {
    private String title;
    private String description;
    private String time;
    private String date;
    
    public ActivityItem(String title, String description, String time, String date) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.date = date;
    }
    
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTime() { return time; }
    public String getDate() { return date; }
}