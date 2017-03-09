package server;

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
    SUNDAY(6);

    private int code;

    private DayOfWeek(int code) {
        this.code = code;
    }

    public static DayOfWeek valueOf(int code) {
        switch (code) {
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
            return DayOfWeek.MONDAY;
        }
    }
}
