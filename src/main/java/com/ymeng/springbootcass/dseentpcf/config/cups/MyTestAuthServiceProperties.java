package com.ymeng.springbootcass.dseentpcf.config.cups;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//@PropertySource("classpath:application-dev.properties")
@ConfigurationProperties(prefix = "vcap.services.mycassauth-service.credentials")
public class MyTestAuthServiceProperties {

    private String cass_username;
    private String cass_password;
    private String truststore_pass;

    public MyTestAuthServiceProperties() {

    }

    public MyTestAuthServiceProperties(
        String cass_username, String cass_password, String truststore_pass) {

        this.cass_username = cass_username;
        this.cass_password = cass_password;
        this.truststore_pass = truststore_pass;
    }

    public void setCass_username(String cass_username) {
        this.cass_username = cass_username;
    }
    public String getCass_username() {
        return this.cass_username;
    }

    public void setCass_password(String cass_password) {
        this.cass_password = cass_password;
    }
    public String getCass_password() {
        return this.cass_password;
    }

    public void setTruststore_pass(String truststore_pass) { this.truststore_pass = truststore_pass; }
    public String getTruststore_pass() {
        return this.truststore_pass;
    }
}
