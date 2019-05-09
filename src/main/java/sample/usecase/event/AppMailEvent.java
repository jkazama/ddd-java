package sample.usecase.event;

import lombok.*;
import sample.context.Dto;

/**
 * Represents a mail delivery event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppMailEvent<T> implements Dto {
    private static final long serialVersionUID = 1L;
    
    private AppMailType mailType;
    private T value;
    
    public static <T> AppMailEvent<T> of(AppMailType mailType, T value) {
        return new AppMailEvent<T>(mailType, value);
    }
    
    public static enum AppMailType {
        FinishRequestWithdraw;
    }
}
