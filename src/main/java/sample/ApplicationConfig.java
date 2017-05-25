package sample;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * アプリケーションにおけるBean定義を表現します。
 * <p>クラス側でコンポーネント定義していない時はこちらで明示的に記載してください。
 *
 * @author jkazama
 */
@Configuration
public class ApplicationConfig {

    /** BeanValidationメッセージのUTF-8に対応したValidator。 */
    @Bean
    public LocalValidatorFactoryBean mvcValidator(MessageSource message) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(message);
        return factory;
    }

}
