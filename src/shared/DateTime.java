package shared;

/**
 * TODO: Describe purpose and behavior of Time
 */
public class DateTime implements Comparable<DateTime> {
    private DayOfWeek day;
    private Time time;

    private DateTime(DayOfWeek day, int totalMinutes) {
        this.day = day;
        this.time = Time.getTimeByTotal(totalMinutes);
    }

    public static DateTime getDateTime(DayOfWeek day, int totalMinutes) {
        if (totalMinutes < 0 || totalMinutes >= Time.MINUTES_PER_DAY) {
            return null;
        }
        return new DateTime(day, totalMinutes);
    }

    public DayOfWeek getDay() {
        return this.day;
    }

    public Time getTime() {
        return this.time;
    }

    @Override
    public int compareTo(DateTime dateTime) {
        int dayDiff = this.day.compareTo(dateTime.day);
        if (dayDiff != 0) {
            return dayDiff;
        }
        return this.time.compareTo(dateTime.time);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(day).append(" ").append(time).toString();
    }
}
