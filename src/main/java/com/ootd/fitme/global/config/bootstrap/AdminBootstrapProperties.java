package com.ootd.fitme.global.config.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "admin.bootstrap")
public class AdminBootstrapProperties {
    private boolean enabled = false;
    private String email;
    private String password;
    private String profileName = "admin";
    private boolean resetPassword = false;
}
