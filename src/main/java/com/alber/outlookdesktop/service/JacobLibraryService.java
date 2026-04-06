package com.alber.outlookdesktop.service;

import com.alber.outlookdesktop.config.JacobProperties;
import com.jacob.com.LibraryLoader;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class JacobLibraryService {

    private static final Logger log = LoggerFactory.getLogger(JacobLibraryService.class);

    private final JacobProperties properties;
    private volatile boolean loaded;

    public JacobLibraryService(JacobProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initialize() {
        String resolvedPath = resolveDllPath();
        if (!StringUtils.hasText(resolvedPath)) {
            throw new OutlookComException(buildMissingDllMessage());
        }
        System.setProperty(LibraryLoader.JACOB_DLL_PATH, resolvedPath);
        loaded = true;
        log.info("JACOB DLL configured at {}", resolvedPath);
    }

    public void ensureLoaded() {
        if (!loaded) {
            throw new OutlookComException(buildMissingDllMessage());
        }
    }

    private String resolveDllPath() {
        String existing = System.getProperty(LibraryLoader.JACOB_DLL_PATH);
        if (StringUtils.hasText(existing)) {
            return existing;
        }

        Set<Path> candidates = new LinkedHashSet<>();
        if (StringUtils.hasText(properties.getDllPath())) {
            candidates.add(Path.of(properties.getDllPath()).toAbsolutePath().normalize());
        }

        String arch = is64Bit() ? "x64" : "x86";
        List<String> names = List.of("jacob.dll", "jacob-" + arch + ".dll", "jacob-1.18-" + arch + ".dll");
        for (String searchPath : properties.getSearchPaths()) {
            Path root = Path.of(searchPath).toAbsolutePath().normalize();
            for (String name : names) {
                candidates.add(root.resolve(name));
            }
        }

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toString();
            }
        }
        return null;
    }

    private String buildMissingDllMessage() {
        String arch = is64Bit() ? "x64" : "x86";
        List<String> searched = new ArrayList<>();
        for (String searchPath : properties.getSearchPaths()) {
            Path root = Path.of(searchPath).toAbsolutePath().normalize();
            searched.add(root.resolve("jacob-" + arch + ".dll").toString());
            searched.add(root.resolve("jacob-1.18-" + arch + ".dll").toString());
            searched.add(root.resolve("jacob.dll").toString());
        }
        return "No se encontro la DLL nativa de JACOB. Coloca la DLL compatible en una de estas rutas: "
                + String.join(", ", searched)
                + " o configura jacob.dll-path / -Djacob.dll.path con la ruta absoluta.";
    }

    private boolean is64Bit() {
        return System.getProperty("os.arch", "").contains("64");
    }
}
