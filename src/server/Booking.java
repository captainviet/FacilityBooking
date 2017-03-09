package server;

import java.util.Comparator;

/**
 * TODO: Describe purpose and behavior of Booking
 */
public class Booking {
    private long confirmationID;
    private Time start;
    private Time end;

    private Booking(long confirmationID, Time start, Time end) {
        this.start = start;
        this.end = end;
        this.confirmationID = confirmationID;
    }

    public static Booking placeBooking(Time start, Time end) {
        if (start.compareTo(end) > 0) {
            return null;
        }
        int confirmationID = (int) System.currentTimeMillis() >> 16;
        return new Booking(confirmationID, start, end);
    }

    public long getConfirmationID() {
        return this.confirmationID;
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
            return true;
        }
        return false;
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
