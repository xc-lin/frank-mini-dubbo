package com.lxc.dubbo.core.protocol.http;

import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.constants.UrlConstant;
import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.reflection.MethodInvocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.HTTP)
public class HttpProviderMethodInvocation {

    @PostMapping(UrlConstant.RPC_URL)
    public Object dubbo(@RequestBody Invocation invocation) throws Exception {
        Object result = MethodInvocation.providerMethodInvocation(invocation);
        return result;
    }


}

