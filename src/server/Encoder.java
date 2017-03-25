package server;

import java.util.InputMismatchException;
import java.util.Scanner;

import shared.DateTime;
import shared.DayOfWeek;
import shared.FreeSlot;
import shared.Time;

/**
 * TODO: Describe purpose and behavior of Encoder
 */
public class Encoder {

    private static Scanner scanner;

    private Encoder() {

    }

    protected static String fromDateTimeToString(DateTime dateTime) {
        StringBuilder str = new StringBuilder().append(dateTime.getDay()).append(" ")
                .append(fromTimeToString(dateTime.getTime()));
        return str.toString();
    }

    protected static DateTime fromStringToDateTime(String str) {
        scanner = new Scanner(str);
        DateTime dateTime;
        try {
            String day = scanner.next();
            String totalMinutes = scanner.next();
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

    protected static String fromFreeSlotToString(FreeSlot freeSlot) {
        StringBuilder str = new StringBuilder().append(fromTimeToString(freeSlot.getStart())).append(" ")
                .append(fromTimeToString(freeSlot.getEnd()));
        return str.toString();
    }

    public static FreeSlot fromStringToFreeSlot(String str) {
        scanner = new Scanner(str);
        FreeSlot freeSlot;
        try {
            int startTotal = scanner.nextInt();
            int endTotal = scanner.nextInt();
            freeSlot = FreeSlot.getFreeSlot(Time.getTimeByTotal(startTotal), Time.getTimeByTotal(endTotal));
        } catch (InputMismatchException e) {
            System.out.println(e.getMessage());
            freeSlot = null;
        }
        return freeSlot;
    }

    protected static String fromTimeToString(Time time) {
        StringBuilder str = new StringBuilder().append(time.getTotalMinutes()).append(" ");
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
