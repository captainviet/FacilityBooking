package shared;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * TODO: Describe purpose and behavior of Encoder
 */
public class Encoder {

    private static Scanner scanner;

    private Encoder() {

    }

    public static String fromDateTimeToString(DateTime dateTime) {
        StringBuilder str = new StringBuilder().append(dateTime.getDay()).append(Constant.DATETIME_DELIM)
                .append(fromTimeToString(dateTime.getTime()));
        return str.toString();
    }

    public static DateTime fromStringToDateTime(String str) {
        scanner = new Scanner(str);
        DateTime dateTime;
        try {
            String dateTimeStr = scanner.next();
            String[] dateTimeComponent = dateTimeStr.split(Constant.DATETIME_DELIM);
            String day = dateTimeComponent[0];
            String totalMinutes = dateTimeComponent[1];
            dateTime = DateTime.getDateTime(DayOfWeek.valueOf(day), Integer.parseInt(totalMinutes));
        } catch (InputMismatchException e) {
            System.out.println(e.getMessage());
            dateTime = null;
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            dateTime = null;
        }
        return dateTime;
    }

    public static String fromFreeSlotToString(FreeSlot freeSlot) {
        StringBuilder str = new StringBuilder().append(fromTimeToString(freeSlot.getStart()))
                .append(Constant.DATETIME_DELIM).append(fromTimeToString(freeSlot.getEnd()));
        return str.toString();
    }

    public static FreeSlot fromStringToFreeSlot(String str) {
        scanner = new Scanner(str);
        FreeSlot freeSlot;
        try {
            String freeSlotStr = scanner.next();
            String[] freeSlotComponent = freeSlotStr.split(Constant.DATETIME_DELIM);
            int startTotal = Integer.parseInt(freeSlotComponent[0]);
            int endTotal = Integer.parseInt(freeSlotComponent[1]);
            freeSlot = FreeSlot.getFreeSlot(Time.getTimeByTotal(startTotal), Time.getTimeByTotal(endTotal));
        } catch (InputMismatchException e) {
            System.out.println(e.getMessage());
            freeSlot = null;
        }
        return freeSlot;
    }

    public static String fromTimeToString(Time time) {
        StringBuilder str = new StringBuilder().append(time.getTotalMinutes());
        return str.toString();
    }

    public static Time fromStringToTime(String str) {
        scanner = new Scanner(str);
        Time time;
        try {
            int totalMinutes = scanner.nextInt();
            time = Time.getTimeByTotal(totalMinutes);
        } catch (InputMismatchException e) {
            System.out.println(e.getMessage());
            time = null;
        }
        return time;
    }

}
