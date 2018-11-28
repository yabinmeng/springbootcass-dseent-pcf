package com.ymeng.springbootcass.dseentpcf.config.cups;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//@PropertySource("classpath:application-dev.properties")
@ConfigurationProperties(prefix = "vcap.services.mycassauth-service.credentials")
public class MyTestAuthServiceProperties {

    private String username;
    private String password;

    public MyTestAuthServiceProperties() {

    }

    public MyTestAuthServiceProperties(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setUsername(String cass_username) {
        this.username = cass_username;
    }
    public String getUsername() {
        return this.username;
    }

    public void setPassword(String cass_password) {
        this.password = cass_password;
    }
    public String getPassword() {
        return this.password;
    }
}
