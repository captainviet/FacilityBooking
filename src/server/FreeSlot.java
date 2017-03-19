package server;

/**
 * TODO: Describe purpose and behavior of FreeSlot
 */
public class FreeSlot {

    protected Time start;
    protected Time end;

    protected FreeSlot(Time start, Time end) {
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

    public boolean isClashed(FreeSlot freeSlot) {
        Time start = freeSlot.start;
        Time end = freeSlot.end;
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

}
