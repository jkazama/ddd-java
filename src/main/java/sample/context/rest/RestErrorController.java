package sample.context.rest;

import java.util.Map;

import org.springframework.boot.web.servlet.error.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Controller which performs exception handling for RestController.
 * <p>Enable it in combination with "error.path" attribute of application.yml.
 * It is necessary to destroy whitelabel in total. "error.whitelabel.enabled: false"
 * <p>see ErrorMvcAutoConfiguration
 */
@RestController
public class RestErrorController implements ErrorController {
    public static final String PATH_ERROR = "/error";

    private final ErrorAttributes errorAttributes;

    public RestErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @Override
    public String getErrorPath() {
        return PATH_ERROR;
    }

    @RequestMapping(PATH_ERROR)
    public Map<String, Object> error(ServletWebRequest request) {
        return this.errorAttributes.getErrorAttributes(request, false);
    }

}
