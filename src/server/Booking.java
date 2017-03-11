package server;

import java.util.Comparator;
import java.util.HashMap;

/**
 * TODO: Describe purpose and behavior of Booking
 */
public class Booking {

    private int confirmationID;
    private Time start;
    private Time end;

    private static HashMap<Integer, Facility> index = new HashMap<>();

    private Booking(int confirmationID, Time start, Time end) {
        this.start = start;
        this.end = end;
        this.confirmationID = confirmationID;
    }

    public static Booking placeBooking(int confirmationID, Time start, Time end) {
        if (start.compareTo(end) > 0) {
            return null;
        }
        return new Booking(confirmationID, start, end);
    }

    public static void setConfirmationToFacility(int confirmationID, Facility facility) {
        index.put(confirmationID, facility);
    }

    public static Facility getFacilityBookedByID(int confirmationID) {
        return index.get(confirmationID);
    }

    public boolean isConfirmationIDEqual(int confirmationID) {
        return this.confirmationID == confirmationID;
    }

    public void setStartTime(Time start) {
        this.start = start;
    }

    public void setEndTime(Time end) {
        this.end = end;
    }

    public Time getStartTime() {
        return this.start;
    }

    public Time getEndTime() {
        return this.end;
    }

    public boolean isClashed(Booking booking) {
        Time start = booking.getStartTime();
        Time end = booking.getEndTime();
        if (this.start.compareTo(start) < 0 && this.end.compareTo(start) < 0
                || this.start.compareTo(end) > 0 && this.end.compareTo(end) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(start).append(" -> ").append(end).toString();
    }

    static class BookingComparator implements Comparator<Booking> {

        @Override
        public int compare(Booking o1, Booking o2) {
            // TODO Auto-generated method stub
            Time start1 = o1.getStartTime();
            Time start2 = o2.getStartTime();
            return start1.compareTo(start2);
        }

    }
}
