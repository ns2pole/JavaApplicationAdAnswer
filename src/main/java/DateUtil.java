import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DateUtil {

    //prac29
    /**
     * 指定日が月曜から金曜かどうかを返す
     *
     * @param yyyymmdd format "YYYYMMDD".
     * @return {@code true} もし引数の日が月曜から金曜なら, {@code false} その他
     * @throws ParseException 日付がYYYYMMDDの形式でない時
     */
    public static boolean isWeekDay(String yyyymmdd) throws ParseException {
        final Calendar calendar = getCalendarFor(yyyymmdd);
        final int TUESDAY_CODE = 2;
        final int FRIDAY_CODE = 6;
        final int dayOfWeekCode = calendar.get(Calendar.DAY_OF_WEEK);
        if(TUESDAY_CODE <= dayOfWeekCode && dayOfWeekCode <= FRIDAY_CODE ) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSaturdayOrSunday(String yyyymmdd) throws ParseException {
        return !isWeekDay(yyyymmdd);
    }
    public static String[] getNationalHoliday(int yyyy) throws URISyntaxException, IOException, InterruptedException  {
        final String responseBody = getResponseBodyTo("https://holidays-jp.github.io/api/v1/" + yyyy + "/date.json");
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(responseBody);
        return getKeyStringArr(jsonNode);
    }

    private static String[] getKeyStringArr(JsonNode jsonNode) {
        final int size = jsonNode.size();
        final String[] arr = new String[size];
        final Iterator<String> iterator = jsonNode.fieldNames();
        for (int i = 0; iterator.hasNext(); i++) {
            arr[i] = iterator.next().replace("-", "/");
        }
        return arr;
    }

    public static String getResponseBodyTo(String url) throws URISyntaxException, IOException, InterruptedException {
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static boolean isNationalHoliday(String yyyymmdd) throws URISyntaxException, IOException, InterruptedException {
        final int year = getYearOf(yyyymmdd);
        final String[] nationalHolidays = getNationalHoliday(year);
        final ArrayList<String> arrDates = new ArrayList<>(Arrays.asList(nationalHolidays));
        return arrDates.contains(yyyymmdd);
    }

    private static int getYearOf(String yyyymmdd) {
        return Integer.parseInt(yyyymmdd.substring(0, 4));
    }

    private static Calendar getCalendarFor(String yyyymmdd) throws ParseException {
        final Date date = DateUtil.validateAndParseDate(yyyymmdd);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static boolean isHoliday(String yyyymmdd) throws URISyntaxException, IOException, InterruptedException, ParseException {
        return isNationalHoliday(yyyymmdd) || isSaturdayOrSunday(yyyymmdd);
    }

    public static String toYYYYMMDD(Calendar c) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String yyyymmdd = sdf.format(c.getTime());
        return yyyymmdd;
    }


    public static Date validateAndParseDate(String inputDate) throws ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        sdf.setLenient(false);
        return sdf.parse(inputDate);
    }

    public static Date addDaysToDate(Date date, int days) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    public static int countWorkingDays(String from, String to) throws URISyntaxException, IOException, InterruptedException, ParseException {
        String[] daysStrings = getDaysStrBetween(from, to);
        int count = 0;
        //toの日付は含めないように変更した為、daysStrings.length - 1までloop
        for (int i = 0; i < daysStrings.length - 1; i++) {
            if(!isHoliday(daysStrings[i]))
            count++;
        }
        if(0 < countDaysBetween(from, to)) {
            return count;
        } else {
            return -1 * count;
        }
    }

    public static String[] getDaysStrBetween(String from, String to) throws ParseException{
        int howManyDays = DateUtil.countDaysBetween(from, to) + 1;
        String[] strings = new String[howManyDays];
        int n = 0;
        Calendar cal = getCalendarFor(from);
        while (n != howManyDays) {
            Calendar tmp = (Calendar) cal.clone();
            tmp.add(Calendar.DAY_OF_MONTH, n);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String formattedDate = sdf.format(tmp.getTime());
            strings[n] = formattedDate;
            n++;
        }
        return strings;
    }

    public static int countDaysBetween(String date1, String date2) throws ParseException {
        Date d1 = validateAndParseDate(date1);
        Date d2 = validateAndParseDate(date2);
        long difference = d2.getTime() - d1.getTime();
        return (int) ( difference / (24 * 60 * 60 * 1000));
    }

    public static String getNextWorkingDayOf(String date) throws ParseException, URISyntaxException, IOException, InterruptedException {
        Calendar dateCal = getCalendarFor(date);
        int n = 1;
        Calendar trialCal = (Calendar) dateCal.clone();
        trialCal.add(Calendar.DAY_OF_MONTH, n);
        String yyyymmdd = toYYYYMMDD(trialCal);
        while (isHoliday(yyyymmdd)) {
            trialCal.add(Calendar.DAY_OF_MONTH, 1);
            yyyymmdd = toYYYYMMDD(trialCal);
        }
        return yyyymmdd;
    }

    public static List<String> getLastMonthDates(String inputDate) throws ParseException {
        ArrayList<String> dates = new ArrayList<>();
        dates.add(inputDate);
        String[] dateParts = inputDate.split("/");
        int year = Integer.parseInt(dateParts[0]);
        //注意：例えば"2023/03/04"がinputDateであれば、2023/02/05までの日付のリストを返したいから、何日まであるのかを見る月は3-1=2で2月にする必要がある。
        int month = Integer.parseInt(dateParts[1]) - 1;
        int howManyDays = getHowManyDaysOf(year, month);
        for(int i = -1; i > -1 * howManyDays - 1; i --) {
            Date date = DateUtil.addDaysToDate(DateUtil.validateAndParseDate(inputDate),i);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String formattedDate = sdf.format(date);
            dates.add(formattedDate);
        }
        return dates;
    }


    private static boolean isLeapYear(int year) {
        if(year % 4 == 0) return true;
        else return false;
    }

    private static int getHowManyDaysOf(int year, int month) {
        if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12 || month == 0) {
            return 31;
        } else if(month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else {
            if(isLeapYear(year)) {
                return 29;
            } else {
                return 28;
            }
        }
    }

}