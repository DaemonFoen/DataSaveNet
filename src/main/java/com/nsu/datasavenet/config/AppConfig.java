package com.nsu.datasavenet.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.config", ignoreUnknownFields = false)
public record AppConfig(@DefaultValue(value = "false") boolean serverAvailable,
                        @DefaultValue(value = "-1") long limitSpace,
                        @NotEmpty String login,
                        @NotEmpty String password) {

    public AppConfig {

    }
}
