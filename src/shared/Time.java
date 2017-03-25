package shared;

/**
 * TODO: Describe purpose and behavior of Time
 */
public class Time implements Comparable<Time> {

    public static final Time END_OF_DAY = new Time(23, 59);
    public static final Time START_OF_DAY = new Time(0, 0);
    public static final int MINUTES_PER_DAY = 1440;

    private int hour;
    private int minute;
    private int totalMinutes;

    private Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.totalMinutes = hour * 60 + minute;
    }

    private Time(int totalMinutes) {
        this.hour = totalMinutes / 60;
        this.minute = totalMinutes % 60;
        this.totalMinutes = totalMinutes;
    }

    public static Time getTime(int hour, int minute) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return new Time(hour, minute);
    }

    public static Time getTimeByTotal(int totalMinutes) {
        if (totalMinutes < 0 || totalMinutes >= MINUTES_PER_DAY) {
            return null;
        }
        return new Time(totalMinutes);
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public int getTotalMinutes() {
        return this.totalMinutes;
    }

    public Time addOffset(int minute) {
        int newMinutes = (this.totalMinutes + MINUTES_PER_DAY + minute) % MINUTES_PER_DAY;
        return getTime(newMinutes / 60, newMinutes % 60);
    }

    @Override
    public int compareTo(Time time) {
        if (this.totalMinutes == time.totalMinutes) {
            return 0;
        }
        return this.totalMinutes > time.totalMinutes ? 1 : -1;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (hour > 9) {
            str.append("0");
        }
        str.append(hour).append(":");
        if (minute > 9) {
            str.append("0");
        }
        str.append(minute);
        return str.toString();
    }
}
