package com.lxc.dubbo.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Url {
    private String host;

    private String port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Url url = (Url) o;
        return Objects.equals(host, url.host) && Objects.equals(port, url.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    public String getAddressAndPort(){
        return String.format("%s:%s", host, port);
    }
}
