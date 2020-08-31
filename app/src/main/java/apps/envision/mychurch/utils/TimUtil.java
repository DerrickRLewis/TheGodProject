package apps.envision.mychurch.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ray on 6/1/2016.
 */

public class TimUtil {

    public static String formatExpiryDate(long expiry){
        // convert seconds to milliseconds
        Date date = new java.util.Date(expiry*1000L);
        // the format of your date
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // give a timezone reference for formatting (see comment at the bottom)
        sdf.setTimeZone(java.util.TimeZone.getDefault());
        return sdf.format(date);
    }

    public static Date timeStampToFormattedDate(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time * 1000L);
        return c.getTime();
    }

    public static boolean hasElapsed(long stored,long duration){
        long timeAgo = System.currentTimeMillis() - duration;
        return stored < timeAgo;
    }

    public static boolean isValidFutureDate(long date){
        return date > (System.currentTimeMillis()/1000);
    }


    /**
     * method to process progress percentage
     * @param currentDuration
     * @param totalDuration
     * @return
     */
    public static int getProgressPercentage(long currentDuration, long totalDuration){
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        // calculating percentage
        Double percentage =(((double)currentSeconds)/totalSeconds)*100;
        // return percentage
        return percentage.intValue();
    }

    /**
     * method to process current music duration in milliseconds
     * @param progress
     * @param totalDuration
     * @return
     */
    public static int progressToTimer(int progress, int totalDuration) {
        totalDuration = (totalDuration / 1000);
        int currentDuration = (int) ((((double)progress) / 100) * totalDuration);
        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static String timeAgo(long timeStamp) {
        long timeDiffernce;
        String perd = "";
        long unixTime = System.currentTimeMillis() / 1000L;  //get_theme current time in seconds.
        int j;
        String[] periods = {"sec", "min", "hr", "day", "week", "month", "year", "decade"};
        // you may choose to write full time intervals like seconds, minutes, days and so on
        double[] lengths = {60, 60, 24, 7, 4.35, 12, 10};
        timeDiffernce = unixTime - timeStamp;
        String tense = "Ago";
        for (j = 0; timeDiffernce >= lengths[j] && j < lengths.length - 1; j++) {
            timeDiffernce /= lengths[j];
        }
        if (timeDiffernce > 1) {
            perd = periods[j] + 's';
        } else {
            perd = periods[j];
        }
        if(timeDiffernce < 1){
            return "Just Now";
        }
        return timeDiffernce + perd + " ";
    }

    public static String noteTimeAgo(long timeStamp) {
        long timeDiffernce;
        String perd = "";
        long unixTime = System.currentTimeMillis()/1000;  //get_theme current time in seconds.
        int j;
        String[] periods = {"sec", "min", "hr", "day", "week", "month", "year", "decade"};
        // you may choose to write full time intervals like seconds, minutes, days and so on
        double[] lengths = {60, 60, 24, 7, 4.35, 12, 10};
        timeDiffernce = unixTime - (timeStamp/1000);
        String tense = "Ago";
        for (j = 0; timeDiffernce >= lengths[j] && j < lengths.length - 1; j++) {
            timeDiffernce /= lengths[j];
        }
        if (timeDiffernce > 1) {
            perd = periods[j] + 's';
        } else {
            perd = periods[j];
        }
        if(timeDiffernce < 1){
            return "Just Now";
        }
        return timeDiffernce + perd + " ";
    }
}