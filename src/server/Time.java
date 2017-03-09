package server;

/**
 * TODO: Describe purpose and behavior of Time
 */
public class Time {

    public static final Time END_OF_DAY = new Time(23, 59);
    public static final Time START_OF_DAY = new Time(0, 0);

    private int hour;
    private int minute;
    private int intValue;

    protected Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.intValue = hour * 60 + minute;
    }

    public static Time getTime(int hour, int minute) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return new Time(hour, minute);
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public int compareTo(Time time2) {
        if (this.intValue == time2.intValue) {
            return 0;
        }
        return this.intValue > time2.intValue ? 1 : -1;
    }
}
