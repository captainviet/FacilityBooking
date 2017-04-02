package server;

import java.util.Comparator;
import java.util.HashMap;

import shared.FreeSlot;
import shared.Time;

/**
 * TODO: Describe purpose and behavior of Booking
 */
public class Booking extends FreeSlot {

    private int confirmationID;

    private static HashMap<Integer, Facility> index = new HashMap<>();

    private Booking(int confirmationID, Time start, Time end) {
        super(start, end);
        this.confirmationID = confirmationID;
    }

    protected static Booking placeBooking(int confirmationID, Time start, Time end) {
        if (start.compareTo(end) > 0) {
            return null;
        }
        return new Booking(confirmationID, start, end);
    }

    protected static void setConfirmationToFacility(int confirmationID, Facility facility) {
        index.put(confirmationID, facility);
    }

    protected static Facility getFacilityBookedByID(int confirmationID) {
        return index.get(confirmationID);
    }

    protected static boolean checkBookingExists(int confirmationID) {
        if (index.containsKey(confirmationID)) {
            return true;
        }
        return false;
    }

    protected static void removeBooking(int confirmationID) {
        // already checked before remove so no need to return anything
        index.remove(confirmationID);
    }

    public boolean isConfirmationIDEqual(int confirmationID) {
        return this.confirmationID == confirmationID;
    }

    protected void setStartTime(Time start) {
        this.start = start;
    }

    protected void setEndTime(Time end) {
        this.end = end;
    }

    public Time getStartTime() {
        return this.start;
    }

    public Time getEndTime() {
        return this.end;
    }

    protected static class BookingComparator implements Comparator<Booking> {

        @Override
        public int compare(Booking o1, Booking o2) {
            // TODO Auto-generated method stub
            Time start1 = o1.getStartTime();
            Time start2 = o2.getStartTime();
            return start1.compareTo(start2);
        }
    }
}
