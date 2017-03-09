package server;

/**
 * TODO: Describe purpose and behavior of Time
 */
public class DateTime {
    private DayOfWeek day;
    private Time time;

    private DateTime(int day, int hour, int minute) {
        this.day = DayOfWeek.valueOf(day);
        this.time = Time.getTime(hour, minute);
    }

    public static DateTime getTime(int day, int hour, int minute) {
        if (day < 0 || day > 6 || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return new DateTime(day, hour, minute);
    }

    public DayOfWeek getDay() {
        return this.day;
    }

    public Time getTime() {
        return this.time;
    }

    public int compareTo(DateTime time2) {
        int dayDiff = this.day.compareTo(time2.day);
        if (dayDiff != 0) {
            return dayDiff;
        }
        return this.time.compareTo(time2.time);
    }
}
