import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Useful methods which I use multiple times
 * 
 * @author hja1g11
 * 
 */
class Tools {

	/**
	 * Returns date and time in format "dd/MM/yyyy HH:mm" as String
	 * 
	 * @return string with date and time
	 */
	public static String getTime() {
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return formatter.format(now);
	}
}