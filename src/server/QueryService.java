package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * TODO: Describe purpose and behavior of QueryService
 */
public class QueryService {

    private static List<Facility> facilities = new ArrayList<>();
    private static List<Booking> bookings = new ArrayList<>();

    private QueryService() {

    }

    public static void setFacilityList(List<Facility> list) {
        facilities = list;
    }

    public static void setBookingList(List<Booking> list) {
        bookings = list;
    }

    public static List<Facility> getAllFacility() {
        return facilities;
    }

    public static List<FreeSlot> getAvailableFacility(String name, DayOfWeek day) {
        Facility facility = Facility.getFacilityByName(name);
        HashMap<DayOfWeek, List<Booking>> timetable = facility.getTimetable();
        List<Booking> booked = timetable.get(day);
        List<FreeSlot> slots = new ArrayList<>();
        if (booked.isEmpty()) {
            slots.add(FreeSlot.getFreeSlot(Time.START_OF_DAY, Time.END_OF_DAY));
        } else {
            Collections.sort(booked, new Booking.BookingComparator());
            Booking firstBooking = booked.get(0);
            slots.add(FreeSlot.getFreeSlot(Time.START_OF_DAY, firstBooking.getStartTime()));
            Booking lastBooking = booked.get(booked.size() - 1);
            slots.add(FreeSlot.getFreeSlot(lastBooking.getEndTime(), Time.END_OF_DAY));
            int size = booked.size();
            if (size > 1) {
                for (int i = 0; i < size - 1; i++) {
                    slots.add(FreeSlot.getFreeSlot(booked.get(i).getEndTime(), booked.get(i + 1).getStartTime()));
                }
            }
        }
        return slots;
    }

    public static long getConfirmationID(String name, DateTime start, DateTime end) {
        long confirmationID = -1;
        DayOfWeek startDay = start.getDay();
        DayOfWeek endDay = end.getDay();
        Facility facility = Facility.getFacilityByName(name);
        HashMap<DayOfWeek, List<Booking>> timetable = facility.getTimetable();
        if (startDay.compareTo(endDay) == -1) {
            Booking bS = Booking.placeBooking(start.getTime(), Time.END_OF_DAY);
            Booking bE = Booking.placeBooking(Time.START_OF_DAY, end.getTime());
            if (bS == null || bE == null) {
                confirmationID = -1;
            } else {
                List<Booking> bookingsStart = timetable.get(startDay);
                List<Booking> bookingsEnd = timetable.get(endDay);
                if (isClashed(bS, bookingsStart) || isClashed(bE, bookingsEnd)) {
                    confirmationID = -1;
                } else {
                    bookingsStart.add(bS);
                    bookingsEnd.add(bE);
                    confirmationID = bS.getConfirmationID();
                }
            }
        } else {
            Booking b = Booking.placeBooking(start.getTime(), end.getTime());
            if (b == null) {
                confirmationID = -1;
            } else {
                List<Booking> bookings = timetable.get(startDay);
                if (isClashed(b, bookings)) {
                    confirmationID = -1;
                } else {
                    bookings.add(b);
                    confirmationID = b.getConfirmationID();
                }
            }
        }
        return confirmationID;
    }

    private static boolean isClashed(Booking booking, List<Booking> bookings) {
        for (Booking b : bookings) {
            if (booking.isClashed(b)) {
                return true;
            }
        }
        return false;
    }

}
