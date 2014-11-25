package sample.context.boot;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * SpringMVCにおいて常に標準Validator(ValidationMessages)を利用してしまう問題に対する対処。
 * 
 * @author jkazama
 */
@Configuration
@EnableWebMvc
@EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
public class WebMvcAutoConfigurationAdapter extends
		org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {

	@Autowired
	private Validator validator;

	@Override
	public org.springframework.validation.Validator getValidator() {
		return new SpringValidatorAdapter(validator);
	}

}
