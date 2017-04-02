package shared;

/**
 * TODO: Describe purpose and behavior of ErrorCode
 */
public enum ErrorCode {
    ERROR_INVALID_FACILITY_NAME("0", "Invalid facility name"),
    ERROR_BOOKING_CLASHED("1", "Booking time clashed"),
    ERROR_BOOKING_NOT_EXIST("2", "Booking does not exist");

    private String code;
    private String msg;

    private ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ErrorCode getError(String code) {
        switch (code) {
        case "0":
            return ERROR_INVALID_FACILITY_NAME;
        case "1":
            return ERROR_BOOKING_CLASHED;
        case "2":
            return ERROR_BOOKING_NOT_EXIST;
        default:
            return null;
        }
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return msg;
    }
}
