package com.lxc.dubbo.invocation;

import com.lxc.dubbo.domain.enums.HealthEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthResponse {
    @GetMapping("/frankDubbo1998-01-20-health")
    public Integer health(){
        return HealthEnum.HEALTH.getStatus();
    }
}
