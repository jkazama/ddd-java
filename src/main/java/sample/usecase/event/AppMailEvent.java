package sample.usecase.event;

import lombok.Builder;
import sample.context.Dto;
import sample.usecase.event.type.AppMailType;

/**
 * Represents a mail delivery event.
 */
@Builder
public record AppMailEvent<T>(
        AppMailType mailType,
        T value) implements Dto {

    public static <T> AppMailEvent<T> of(AppMailType mailType, T value) {
        return AppMailEvent.<T>builder()
                .mailType(mailType)
                .value(value)
                .build();
    }

}
