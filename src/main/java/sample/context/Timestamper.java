package sample.context;

import java.util.*;

import org.springframework.stereotype.Component;

import sample.util.*;

/**
 * Date and time utility component.
 */
@Component
public class Timestamper {

    // low: In the sample, fixed business days (20141118) are set when initializing DataFixtures.
    private String day;

    public Timestamper() {
        this(DateUtils.dayFormat(new Date()));
    }
    
    public Timestamper(String day) {
        this.day = day;
    }

    public String day() {
        return day;
    }

    public Date date() {
        return new Date();
    }

    public TimePoint tp() {
        return new TimePoint(day(), date());
    }

    public Timestamper daySet(String day) {
        this.day = day;
        return this;
    }

    //low: T + n calculation method for sample. In fact, it is necessary to consider business days including holidays
    public String dayPlus(int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateUtils.date(day));
        cal.add(Calendar.DAY_OF_MONTH, i);
        return DateUtils.dayFormat(cal.getTime());
    }

}
