package sample.model.constraints;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import javax.validation.*;
import javax.validation.constraints.*;

/**
 * 各種カテゴリ/区分(必須)を表現する制約注釈。
 * 
 * @author jkazama
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotNull
@Size
@Pattern(regexp = "")
public @interface Category {
	String message() default "{error.domain.category}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@OverridesAttribute(constraint = Size.class, name = "max")
	int max() default 30;

	@OverridesAttribute(constraint = Pattern.class, name = "regexp")
	String regexp() default "^\\p{ASCII}*$";

	@OverridesAttribute(constraint = Pattern.class, name = "flags")
	Pattern.Flag[] flags() default {};

	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Category[] value();
	}
}

