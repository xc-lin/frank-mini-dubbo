package com.lxc.dubbo.core.invocation;

import com.lxc.dubbo.core.domain.constants.UrlConstant;
import com.lxc.dubbo.core.domain.enums.HealthEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Deprecated
public class HealthResponse {
    @GetMapping(UrlConstant.HEALTH_URL)
    public Integer health(){
        return HealthEnum.HEALTH.getStatus();
    }
}
