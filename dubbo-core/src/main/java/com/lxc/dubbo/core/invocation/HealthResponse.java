package com.lxc.dubbo.core.invocation;

import com.lxc.dubbo.domain.constants.UrlConstants;
import com.lxc.dubbo.domain.enums.HealthEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthResponse {
    @GetMapping(UrlConstants.HEALTH_URL)
    public Integer health(){
        return HealthEnum.HEALTH.getStatus();
    }
}
