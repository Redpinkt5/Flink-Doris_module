package cn.rp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Rp
 * @date 2026.6.9 18:58
 **/
public class DateUtil {
    public static String getDay(long time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String result = simpleDateFormat.format(new Date(time));
        return result;
    }

    public static String getHour(long time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH");
        String result = simpleDateFormat.format(new Date(time));
        return result;
    }

    public static String getDay(Date time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String result = simpleDateFormat.format(time);
        return result;
    }

    public static String getHour(Date time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH");
        String result = simpleDateFormat.format(time);
        return result;
    }

    public static int compareDate(String date1, String date2,String dateformat){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateformat);
        try {
            Date date22 = simpleDateFormat.parse(date2);
            Date date11 = simpleDateFormat.parse(date1);
            return date11.compareTo(date22);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println(compareDate("20210908","20210910","yyyyMMdd"));
    }
}









