package shared;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Describe purpose and behavior of DayOfWeek
 */
public enum DayOfWeek {
    MONDAY(0),
    TUESDAY(1),
    WEDNESDAY(2),
    THURSDAY(3),
    FRIDAY(4),
    SATURDAY(5),
    SUNDAY(6),
    INVALID(7);

    private int code;

    private DayOfWeek(int code) {
        this.code = code;
    }

    public List<DayOfWeek> getDaysInbetween(DayOfWeek day) {
        int start = this.code;
        int end = day.code;
        List<DayOfWeek> inbetween = new ArrayList<>();
        if (end - start > 1) {
            for (int i = start + 1; i <= end - 1; i++) {
                inbetween.add(DayOfWeek.valueOf(i));
            }
        }
        return inbetween;
    }

    public DayOfWeek getOffsetDay(int offset) {
        int newCode = this.code + offset;
        if (newCode > 6 || newCode < 0) {
            return null;
        }
        return valueOf(newCode);
    }

    public static DayOfWeek valueOf(int code) {
        switch (code) {
        case 0:
            return DayOfWeek.MONDAY;
        case 1:
            return DayOfWeek.TUESDAY;
        case 2:
            return DayOfWeek.WEDNESDAY;
        case 3:
            return DayOfWeek.THURSDAY;
        case 4:
            return DayOfWeek.FRIDAY;
        case 5:
            return DayOfWeek.SATURDAY;
        case 6:
            return DayOfWeek.SUNDAY;
        default:
            return DayOfWeek.INVALID;
        }
    }
}
