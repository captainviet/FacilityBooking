package client;

public class Validator {
	public static String validateListOfDay(String days){
		String[] dayArr = days.split(",");
		for (String day : dayArr) {
			try {
				Integer.parseInt(day);
			} catch (NumberFormatException ne) {
				return ne.getMessage();
			}
		}
		return null;
	}
}
