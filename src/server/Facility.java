package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shared.DayOfWeek;

/**
 * TODO: Describe purpose and behavior of Facility
 */
public class Facility {
    private String name;
    private HashMap<DayOfWeek, List<Booking>> timetable = new HashMap<>();
    {
        timetable.put(DayOfWeek.MONDAY, new ArrayList<>());
        timetable.put(DayOfWeek.TUESDAY, new ArrayList<>());
        timetable.put(DayOfWeek.WEDNESDAY, new ArrayList<>());
        timetable.put(DayOfWeek.THURSDAY, new ArrayList<>());
        timetable.put(DayOfWeek.FRIDAY, new ArrayList<>());
        timetable.put(DayOfWeek.SATURDAY, new ArrayList<>());
        timetable.put(DayOfWeek.SUNDAY, new ArrayList<>());
    }

    private static HashMap<String, Facility> index = new HashMap<>();

    private Facility(String name) {
        this.name = name;
    }

    public static void initialize() {
        String LT = "LT";
        String TR = "TR";
        for (int i = 1; i <= 10; i++) {
            addFacility(LT + i);
            addFacility(TR + i);
        }
    }

    public String getFacilityName() {
        return this.name;
    }

    protected List<Booking> getTimetableOn(DayOfWeek day) {
        return this.timetable.get(day);
    }

    protected boolean cancelBooking(DayOfWeek day, Booking booking) {
        if (timetable.get(day).contains(booking)) {
            this.timetable.get(day).remove(booking);
            return true;
        }
        return false;
    }

    protected static boolean addFacility(String name) {
        if (index.containsKey(name)) {
            return false;
        }
        index.put(name, new Facility(name));
        return true;
    }

    protected static Facility getFacilityByName(String name) {
        return index.get(name);
    }

    protected static List<Facility> getAllFacility() {
        List<Facility> facilities = new ArrayList<>();
        facilities.addAll(index.values());
        return facilities;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Facility Name: ").append(name).toString();
    }
}
