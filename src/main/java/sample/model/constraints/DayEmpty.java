package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import javax.validation.*;
import javax.validation.constraints.*;

/**
 * 日付を表現する制約注釈。
 * <p>yyyyMMddの8桁文字列を想定します。
 * 
 * @author jkazama
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@Size
@Pattern(regexp = "")
public @interface DayEmpty {
	String message() default "{error.domain.day}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@OverridesAttribute(constraint = Size.class, name = "max")
	int max() default 8;

	@OverridesAttribute(constraint = Pattern.class, name = "regexp")
	String regexp() default "^\\d{8}|\\d{0}$";

	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		DayEmpty[] value();
	}
}
