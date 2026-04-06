package com.alber.outlookdesktop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "jacob")
public class JacobProperties {

    private String dllPath;
    private List<String> searchPaths = new ArrayList<>(List.of("lib", ".", "target"));

    public String getDllPath() {
        return dllPath;
    }

    public void setDllPath(String dllPath) {
        this.dllPath = dllPath;
    }

    public List<String> getSearchPaths() {
        return searchPaths;
    }

    public void setSearchPaths(List<String> searchPaths) {
        this.searchPaths = searchPaths;
    }
}
