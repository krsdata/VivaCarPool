package au.com.vivacar.vivacarpool.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by adarsh on 23/3/17.
 */

public class DateTimeUtil {
    public static String formatDateForDisplay(String date){

        Date textToDate = new Date();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            textToDate = dbFormat.parse(date.split("T")[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy");
        return displayFormat.format(textToDate);
    }

    public static Integer getTimeInMinutes(String time){

        String[] timeArray = time.split(":");

        Integer hoursAsMinutes = Integer.valueOf(timeArray[0])*60;
        Integer minutes = Integer.valueOf(timeArray[1]);

        return hoursAsMinutes+minutes;
    }

    public static String formatDisplayforTime(Integer timeInMinutes){

        Integer hours = timeInMinutes/60;
        Integer minutes = timeInMinutes%60;

        return hours+":"+minutes;
    }
}
