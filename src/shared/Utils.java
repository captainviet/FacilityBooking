package shared;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
	public static String currentLogFormatTime() {
    	SimpleDateFormat format = new SimpleDateFormat("E h:m");
    	return format.format(Calendar.getInstance().getTime());
    }
}
