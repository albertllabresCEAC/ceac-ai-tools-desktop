package com.alber.outlookdesktop;

import com.alber.outlookdesktop.config.JacobProperties;
import com.alber.outlookdesktop.config.OutlookProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = {OutlookProperties.class, JacobProperties.class})
public class OutlookDesktopComMcpApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OutlookDesktopComMcpApplication.class)
                .headless(false)
                .run(args);
    }
}
