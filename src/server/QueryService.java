package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shared.DateTime;
import shared.DayOfWeek;
import shared.FreeSlot;
import shared.Time;

/**
 * TODO: Describe purpose and behavior of QueryService
 */
public class QueryService {

    private static final int ADVANCE_MINUTE_LIMIT = -60;
    private static final int POSTPONE_MINUTE_LIMIT = 30;

    private QueryService() {

    }

    public static void initialize() {
        String LT = "LT";
        String TR = "TR";
        for (int i = 1; i <= 10; i++) {
            Facility.addFacility(LT + i);
            Facility.addFacility(TR + i);
        }
    }

    private enum BookingEditMode {
        ADVANCE,
        POSTPONE
    }

    public static List<FreeSlot> getAvailableFacility(Facility facility, DayOfWeek day) {
        // get list of bookings for the facility in the day & initialize an empty list of free slots
        List<Booking> booked = facility.getTimetableOn(day);
        List<FreeSlot> slots = new ArrayList<>();
        if (booked.isEmpty()) {
            // if there's no booking, indicate a free slot as the whole day
            slots.add(FreeSlot.getFreeSlot(Time.START_OF_DAY, Time.END_OF_DAY));
        } else {
            // else add the free slot before the first booking & after the last booking (the two may be just one booking)
            Collections.sort(booked, new Booking.BookingComparator());
            Booking firstBooking = booked.get(0);
            if (firstBooking != null) {
                slots.add(FreeSlot.getFreeSlot(Time.START_OF_DAY, firstBooking.getStartTime()));
            }
            Booking lastBooking = booked.get(booked.size() - 1);
            if (lastBooking != null) {
                slots.add(FreeSlot.getFreeSlot(lastBooking.getEndTime(), Time.END_OF_DAY));
            }
            int size = booked.size();
            if (size > 1) {
                // if there's more than one booking, add all the gap between bookings as free slots
                for (int i = 0; i < size - 1; i++) {
                    slots.add(FreeSlot.getFreeSlot(booked.get(i).getEndTime(), booked.get(i + 1).getStartTime()));
                }
            }
        }
        return slots;
    }

    public static List<FreeSlot> getAvailableFacility(Facility facility, DayOfWeek day, Time start, Time end) {
        // get list of bookings for the facility in the day & initialize an empty list of free slots
        List<Booking> booked = facility.getTimetableOn(day);
        List<FreeSlot> slots = new ArrayList<>();
        if (booked.isEmpty()) {
            slots.add(FreeSlot.getFreeSlot(start, end));
            return slots;
        } else {
            Collections.sort(booked, new Booking.BookingComparator());
            Booking firstBooking = booked.get(0);
            if (firstBooking != null) {
                slots.add(FreeSlot.getFreeSlot(Time.START_OF_DAY, firstBooking.getStartTime()));
            }
            Booking lastBooking = booked.get(booked.size() - 1);
            if (lastBooking != null) {
                slots.add(FreeSlot.getFreeSlot(lastBooking.getEndTime(), Time.END_OF_DAY));
            }
            int size = booked.size();
            if (size > 1) {
                for (int i = 0; i < size - 1; i++) {
                    slots.add(FreeSlot.getFreeSlot(booked.get(i).getEndTime(), booked.get(i + 1).getStartTime()));
                }
            }
            // transfer to a new list those that are in range
            List<FreeSlot> inRange = new ArrayList<>();
            for (FreeSlot slot : slots) {
                if (slot.getEnd().compareTo(start) <= 0 || slot.getStart().compareTo(end) >= 0)
                    continue;
                if (slot.getStart().compareTo(start) <= 0 && slot.getEnd().compareTo(start) > 0) {
                    inRange.add(FreeSlot.getFreeSlot(start, slot.getEnd()));
                    continue;
                }
                if (slot.getStart().compareTo(end) < 0 && slot.getEnd().compareTo(end) >= 0) {
                    inRange.add(FreeSlot.getFreeSlot(slot.getStart(), end));
                    continue;
                }
                inRange.add(slot);
            }
            return inRange;
        }
    }

    public static Map<Facility, List<FreeSlot>> getAllAvailableFacility() {
        List<Facility> facilities = Facility.getAllFacility();
        Map<Facility, List<FreeSlot>> facilitySlots = new HashMap<>();

        for (Facility facility : facilities) {
            List<FreeSlot> slots = new ArrayList<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                slots.addAll(getAvailableFacility(facility, day));
            }
            facilitySlots.put(facility, slots);
        }

        return facilitySlots;
    }

    public static long getConfirmationID(Facility facility, DateTime start, DateTime end) {
        int confirmationID = (int) System.currentTimeMillis() >> 16;
        DayOfWeek startDay = start.getDay();
        DayOfWeek endDay = end.getDay();
        if (startDay.compareTo(endDay) == -1) {
            // if the booking spans multiple days, add the booking for start day and end day first
            Booking bS = Booking.placeBooking(confirmationID, start.getTime(), Time.END_OF_DAY);
            Booking bE = Booking.placeBooking(confirmationID, Time.START_OF_DAY, end.getTime());
            if (bS == null || bE == null) {
                confirmationID = -1;
            } else {
                List<Booking> bookingsStart = facility.getTimetableOn(startDay);
                List<Booking> bookingsEnd = facility.getTimetableOn(endDay);
                if (isClashed(bS, bookingsStart) || isClashed(bE, bookingsEnd)) {
                    confirmationID = -1;
                } else {
                    // if the two bookings don't clash with any other bookings, check for clashes during the days in-between
                    List<DayOfWeek> inbetween = startDay.getDaysInbetween(endDay);
                    boolean free = true;
                    List<List<Booking>> bookingsInbetween = new ArrayList<>();
                    for (DayOfWeek day : inbetween) {
                        List<Booking> bookingsToday = facility.getTimetableOn(day);
                        if (!bookingsToday.isEmpty()) {
                            // if any of the in-between days have booking, cancel the booking
                            free = false;
                            break;
                        }
                        // otherwise retrieve all references to the bookings record to append later
                        bookingsInbetween.add(bookingsToday);
                    }
                    if (free) {
                        // if all the days in-between are free, add the booking for first day, last day and all the days in-between
                        bookingsStart.add(bS);
                        bookingsEnd.add(bE);
                        for (List<Booking> bookings : bookingsInbetween) {
                            bookings.add(Booking.placeBooking(confirmationID, Time.START_OF_DAY, Time.END_OF_DAY));
                        }
                    } else {
                        confirmationID = -1;
                    }
                }
            }
        } else {
            // else if start day and end day is the same day (illegal argument is unlikely), place a single booking and check for clashes before recording the booking
            Booking b = Booking.placeBooking(confirmationID, start.getTime(), end.getTime());
            if (b == null) {
                confirmationID = -1;
            } else {
                List<Booking> bookings = facility.getTimetableOn(startDay);
                if (isClashed(b, bookings)) {
                    confirmationID = -1;
                } else {
                    bookings.add(b);
                }
            }
        }
        // add to the booking index the facility associated with this booking
        Booking.setConfirmationToFacility(confirmationID, facility);
        return confirmationID;
    }

    // possible to have bookings span across weeks?
    public static boolean editBookedConfirmation(int confirmationID, int minute) {
        if (minute == 0) {
            // no changes to current booking
            return true;
        }
        if (minute < ADVANCE_MINUTE_LIMIT || minute > POSTPONE_MINUTE_LIMIT) {
            // offset exceeds what is permitted
            return false;
        }
        // set mode to either postpone or advance
        BookingEditMode editMode = minute > 0 ? BookingEditMode.POSTPONE : BookingEditMode.ADVANCE;
        Facility facility = Booking.getFacilityBookedByID(confirmationID);
        // get all bookings with the same confirmationID as the parameter
        List<Booking> returnBookings = new ArrayList<>();
        // record the startDay of the group of bookings
        DayOfWeek startDay = null;
        for (DayOfWeek day : DayOfWeek.values()) {
            List<Booking> bookings = facility.getTimetableOn(day);
            for (Booking booking : bookings) {
                if (booking.isConfirmationIDEqual(confirmationID)) {
                    returnBookings.add(booking);
                    if (startDay == null) {
                        startDay = day;
                    }
                    break;
                }
            }
        }
        if (returnBookings.isEmpty()) {
            // if there's no booking, indicate false confirmationID
            return false;
        }
        int size = returnBookings.size();
        if (size == 1) {
            // if the booking lies in a single day only
            Booking singleBooking = returnBookings.get(0);
            Time startTime = singleBooking.getStartTime();
            Time endTime = singleBooking.getEndTime();
            List<Booking> todayBookings = facility.getTimetableOn(startDay);
            if (editMode == BookingEditMode.ADVANCE) {
                DayOfWeek yesterday = startDay.getOffsetDay(-1);
                // handling advance mode
                if (startTime.getTotalMinutes() >= Math.abs(minute)) {
                    singleBooking.setStartTime(startTime.addOffset(minute));
                    singleBooking.setEndTime(endTime.addOffset(minute));
                    return true;
                }
                if (yesterday == null) {
                    return false;
                }
                List<Booking> yesterdayBookings = facility.getTimetableOn(yesterday);
                Time newStart = startTime;
                Time newEnd = endTime;
                if (endTime.getTotalMinutes() < Math.abs(minute)) {
                    // if the amount of advancement greater than the end time, move the entire booking to yesterday (null if start day is Monday)
                    newStart = startTime.addOffset(minute);
                    newEnd = endTime.addOffset(minute);
                    todayBookings.remove(singleBooking);
                } else if (startTime.getTotalMinutes() < Math.abs(minute)) {
                    // if the amount of advancement greater than the start time, place another booking to yesterday and set this booking's start time to start of day
                    newStart = startTime.addOffset(minute);
                    newEnd = Time.END_OF_DAY;
                    singleBooking.setStartTime(Time.START_OF_DAY);
                }
                Booking yesterdayBooking = Booking.placeBooking(confirmationID, newStart, newEnd);
                if (isClashed(singleBooking, todayBookings) || isClashed(yesterdayBooking, yesterdayBookings)) {
                    // if operation not successful, revert singleBooking's start time to original start time
                    singleBooking.setStartTime(startTime);
                    return false;
                }
                yesterdayBookings.add(yesterdayBooking);
            } else {
                DayOfWeek tomorrow = startDay.getOffsetDay(1);
                // handling postpone mode
                if (endTime.getTotalMinutes() + minute < Time.MINUTES_PER_DAY) {
                    singleBooking.setStartTime(startTime.addOffset(minute));
                    singleBooking.setEndTime(endTime.addOffset(minute));
                    return true;
                }
                if (tomorrow == null) {
                    return false;
                }
                List<Booking> tomorrowBookings = facility.getTimetableOn(tomorrow);
                Time newStart = startTime;
                Time newEnd = endTime;
                if (startTime.getTotalMinutes() + minute >= Time.MINUTES_PER_DAY) {
                    // if the amount of advancement greater than start time, move the entire booking to tomorrow (null if start day is Sunday)
                    newStart = startTime.addOffset(minute);
                    newEnd = endTime.addOffset(minute);
                    todayBookings.remove(singleBooking);
                } else if (endTime.getTotalMinutes() + minute >= Time.MINUTES_PER_DAY) {
                    newStart = Time.START_OF_DAY;
                    newEnd = endTime.addOffset(minute);
                    singleBooking.setEndTime(Time.END_OF_DAY);
                }
                Booking tomorrowBooking = Booking.placeBooking(confirmationID, newStart, newEnd);
                if (isClashed(singleBooking, todayBookings) || isClashed(tomorrowBooking, tomorrowBookings)) {
                    // if operation not successful, revert singleBooking's end time to original end time
                    singleBooking.setEndTime(endTime);
                    return false;
                }
                tomorrowBookings.add(tomorrowBooking);
            }
        } else {
            // if booking spans multiple days, get the first booking and the last booking
            Booking firstBooking = returnBookings.get(0);
            Booking lastBooking = returnBookings.get(size - 1);
            // get only first booking start time since end time must be end of day
            Time startTime = firstBooking.getStartTime();
            // get only last booking end time since start time must be start of day
            Time endTime = lastBooking.getEndTime();
            List<Booking> startBookings = facility.getTimetableOn(startDay);
            DayOfWeek endDay = startDay.getOffsetDay(size);
            List<Booking> endBookings = facility.getTimetableOn(endDay);
            if (editMode == BookingEditMode.ADVANCE) {
                // handling advance mode
                if (startTime.getTotalMinutes() < Math.abs(minute)) {
                    // if booking needs to span to start yesterday
                    DayOfWeek startYesterday = startDay.getOffsetDay(-1);
                    if (startYesterday == null) {
                        return false;
                    }
                    List<Booking> startYesterdayBookings = facility.getTimetableOn(startYesterday);
                    firstBooking.setStartTime(Time.START_OF_DAY);
                    Booking newBooking = Booking.placeBooking(confirmationID, startTime.addOffset(minute),
                            Time.END_OF_DAY);
                    if (isClashed(firstBooking, startBookings) || isClashed(newBooking, startYesterdayBookings)) {
                        // revert changes
                        firstBooking.setStartTime(startTime);
                        return false;
                    }
                    startYesterdayBookings.add(newBooking);
                } else {
                    firstBooking.setStartTime(startTime.addOffset(minute));
                }
                if (endTime.getTotalMinutes() < Math.abs(minute)) {
                    Booking endYesterdayBooking = returnBookings.get(size - 2);
                    endYesterdayBooking.setEndTime(endTime.addOffset(minute));
                    endBookings.remove(lastBooking);
                } else {
                    lastBooking.setEndTime(endTime.addOffset(minute));
                }
            } else {
                // handling postpone mode
                if (startTime.getTotalMinutes() + minute >= Time.MINUTES_PER_DAY) {
                    Booking startTomorrowBooking = returnBookings.get(1);
                    startTomorrowBooking.setStartTime(startTime.addOffset(minute));
                    startBookings.remove(firstBooking);
                } else {
                    firstBooking.setStartTime(startTime.addOffset(minute));
                }
                if (endTime.getTotalMinutes() + minute >= Time.MINUTES_PER_DAY) {
                    DayOfWeek endTomorrow = endDay.getOffsetDay(1);
                    if (endTomorrow == null) {
                        return false;
                    }
                    List<Booking> endTomorrowBookings = facility.getTimetableOn(endTomorrow);
                    lastBooking.setEndTime(Time.END_OF_DAY);
                    Booking newBooking = Booking.placeBooking(confirmationID, Time.START_OF_DAY,
                            endTime.addOffset(minute));
                    if (isClashed(lastBooking, endBookings) || isClashed(newBooking, endTomorrowBookings)) {
                        lastBooking.setEndTime(endTime);
                        return false;
                    }
                    endTomorrowBookings.add(newBooking);
                } else {
                    lastBooking.setEndTime(endTime.addOffset(minute));
                }
            }
        }
        return true;
    }

    public static boolean cancelBookedConfirmation(int confirmationID) {
        if (Booking.checkBookingExists(confirmationID)) {
            Facility facility = Booking.getFacilityBookedByID(confirmationID);
            // get all bookings with the same confirmationID as the parameter
            for (DayOfWeek day : DayOfWeek.values()) {
                Booking toBeCanceled = null;
                List<Booking> bookings = facility.getTimetableOn(day);
                for (Booking booking : bookings) {
                    if (booking.isConfirmationIDEqual(confirmationID)) {
                        toBeCanceled = booking;
                        break;
                    }
                }
                if (toBeCanceled != null) {
                    // remove in the facility's booking array
                    facility.cancelBooking(day, toBeCanceled);
                }
            }
            // remove the booking from the Booking class
            Booking.removeBooking(confirmationID);
            return true;
        }
        return false;
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
