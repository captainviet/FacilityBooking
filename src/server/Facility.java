package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public String getFacilityName() {
        return this.name;
    }

    public HashMap<DayOfWeek, List<Booking>> getTimetable() {
        return this.timetable;
    }

    public static void addFacility(String name) {
        index.put(name, new Facility(name));
    }

    public static Facility getFacilityByName(String name) {
        return index.get(name);
    }
}
