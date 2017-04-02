package shared;

import java.util.Calendar;

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
    
    public static DateTime now(){
    	Calendar c = Calendar.getInstance();
    	int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
    	DayOfWeek today = DayOfWeek.valueOf(dayOfWeek < 2 ? 6 : dayOfWeek - 2 );
    	int totalMinutes = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    	return DateTime.getDateTime(today, totalMinutes);
    }
    
    public int minutesFrom(DateTime other) {
    	int isAfter = this.compareTo(other);
    	if ( isAfter < 0) {
    		return -1;
    	} else if (isAfter == 0) {
    		return 0;
    	} else {
    		int dayDiff = this.day.getCode() - other.day.getCode();
    		if (dayDiff == 0) {
    			return this.getTime().getTotalMinutes() - other.getTime().getTotalMinutes();
    		} else {
    			return (Time.MINUTES_PER_DAY - other.getTime().getTotalMinutes()) + this.getTime().getTotalMinutes();
    		}
    	}
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
