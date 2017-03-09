package server;

/**
 * TODO: Describe purpose and behavior of FreeSlot
 */
public class FreeSlot {

    private Time start;
    private Time end;

    private FreeSlot(Time start, Time end) {
        this.start = start;
        this.end = end;
    }

    public static FreeSlot getFreeSlot(Time start, Time end) {
        if (start.compareTo(end) > 0) {
            return null;
        }
        return new FreeSlot(start, end);
    }

    public Time getStart() {
        return this.start;
    }

    public Time getEnd() {
        return this.end;
    }

}
