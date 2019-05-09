package sample;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ApplicationConfig {

    /** UTF8 to JSR303 message file. */
    @Bean
    public LocalValidatorFactoryBean defaultValidator(MessageSource message) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(message);
        return factory;
    }

}
