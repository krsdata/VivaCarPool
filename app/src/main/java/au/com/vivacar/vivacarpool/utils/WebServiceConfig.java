package au.com.vivacar.vivacarpool.utils;

/**
 * Created by adarsh on 1/4/17.
 */

public class WebServiceConfig {
    public static String HOST ="http://192.168.1.3:9011/";

    //public static String HOST ="http://128.199.126.197:8080/";

    public static String APPLICATION ="VivaCarpool/";

    public static String SIGNIN = HOST+APPLICATION+"signin";

    public static String ADD_RIDE = HOST+APPLICATION+"addride";

    public static String GET_CARD_DETAILS = HOST+APPLICATION+"getcarddetails";

    public static String GET_PAYMENT_HISTORY = HOST+APPLICATION+"getpaymenthistory";

    public static String UPDATE_RIDE = HOST+APPLICATION+"updateride";

    public static String UPDATE_TOKEN = HOST+APPLICATION+"updatetoken";

    public static String CANCEL_RIDE = HOST+APPLICATION+"cancelride";

    public static String RIDE_DETAIL = HOST+APPLICATION+"ridedetail";

    public static String GET_RIDES_OWNER = HOST+APPLICATION+"getridesasowner";

    public static String GET_RIDES_TRAVELLER = HOST+APPLICATION+"getridesastraveller";

    public static String SEARCH_RIDES = HOST+APPLICATION+"searchrides";

    public static String SIGNUP = HOST+APPLICATION+"signup";

    public static String GET_RIDE_DETAIL = HOST+APPLICATION+"ridedetail";

    public static String BOOK_SEATS = HOST+APPLICATION+"bookseats";

    public static String CANCEL_BOOKING = HOST+APPLICATION+"cancelbooking";

    public static String GET_BOOKINGS_FOR_RIDE = HOST+APPLICATION+"bookingsforride";

    public static String START_RIDE = HOST+APPLICATION+"startride";

    public static String END_RIDE = HOST+APPLICATION+"endride";

    public static String REJECT_BOOKING = HOST+APPLICATION+"rejectreservation";

    public static String CONFIRM_BOOKING = HOST+APPLICATION+"confirmreservation";

    public static String GET_NOTIFICATIONS = HOST+APPLICATION+"getnotifications";

}
