package sample.context;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import sample.util.TimePoint;

/**
 * Date and time utility component.
 */
@Component
public class Timestamper {

    private LocalDate day;

    public Timestamper() {
        this(LocalDate.now());
    }

    public Timestamper(LocalDate day) {
        this.day = day;
    }

    public LocalDate day() {
        return day;
    }

    public LocalDateTime date() {
        return LocalDateTime.now();
    }

    public TimePoint tp() {
        return new TimePoint(day(), date());
    }

    public Timestamper daySet(LocalDate day) {
        this.day = day;
        return this;
    }

    // low: T + n calculation method for sample. In fact, it is necessary to
    // consider business days including holidays
    public LocalDate dayPlus(int i) {
        return this.day.plusDays(i);
    }

}
