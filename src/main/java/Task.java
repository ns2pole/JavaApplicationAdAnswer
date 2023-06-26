import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Task {
    String name;
    String deadLineDate;
    int planMinuteTime;
    int actualMinuteTime;
    Status status;


    public Task(String name, String deadLineDate) {
        this.name = name;
        this.deadLineDate = deadLineDate;
        this.actualMinuteTime = 0;
        this.status = Status.UNHANDLED;
    }

    private int getActualMinuteTime() {
        return this.actualMinuteTime;
    }

    private int getPlanMinuteTime() {
        return this.planMinuteTime;
    }

    //prac27
    public static int getTotalActualTime(Task[] tasks) {
        return Arrays
                .stream(tasks)
                .mapToInt(Task::getActualMinuteTime)
                .sum();
    }

    //prac27
    public static int getTotalPlanTime(Task[] tasks) {
        return Arrays.stream(tasks).mapToInt(Task::getPlanMinuteTime).sum();
    }


    public static int sum(int[] array) {
        return Arrays.stream(array).sum();
    }

    public boolean onDeadLine() throws URISyntaxException, IOException, InterruptedException, ParseException{
        if(this.countToDeadLineDate() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isExpired() throws URISyntaxException, IOException, InterruptedException, ParseException{
        if(this.countToDeadLineDate() < 0) {
            return true;
        } else {
            return false;
        }
    }

    public int countToDeadLineDate() throws URISyntaxException, IOException, InterruptedException, ParseException {
        Calendar todayC = Calendar.getInstance();
        if(todayC.getTime().after(DateUtil.validateAndParseDate(this.deadLineDate))) {
            return -1 * DateUtil.countWorkingDays(this.deadLineDate, DateUtil.toYYYYMMDD(todayC));
        } else {
            return DateUtil.countWorkingDays(DateUtil.toYYYYMMDD(todayC), this.deadLineDate);
        }
    }

}