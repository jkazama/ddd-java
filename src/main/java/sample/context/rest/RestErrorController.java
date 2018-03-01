package sample.context.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * REST用の例外ハンドリングを行うController。
 * <p>application.ymlの"error.path"属性との組合せで有効化します。
 * あわせて"error.whitelabel.enabled: false"でwhitelabelを無効化しておく必要があります。
 * 
 * @author jkazama
 */
@RestController
public class RestErrorController implements ErrorController {
    public static final String PATH_ERROR = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return PATH_ERROR;
    }

    @RequestMapping(PATH_ERROR)
    public Map<String, Object> error(ServletWebRequest request) {
        return this.errorAttributes.getErrorAttributes(request, false);
    }

}
