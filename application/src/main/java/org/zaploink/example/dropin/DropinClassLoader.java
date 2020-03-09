package org.zaploink.example.dropin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

@Component
@ConditionalOnProperty("dropin.directory")
public class DropinClassLoader extends URLClassLoader {

    private static final Logger LOG = Logger.getLogger(DropinClassLoader.class.getName());

    @Autowired
    public DropinClassLoader(@Value("${dropin.directory:}") String dropinDirectory) throws IOException {
        super(getExtJarUrls(dropinDirectory), Thread.currentThread().getContextClassLoader());
    }

    private static URL[] getExtJarUrls(String dropinDirectory) throws IOException {
        Path dir = Paths.get(dropinDirectory);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            LOG.severe("Configured drop-in directory '"+dropinDirectory+"' does not exist or is not a directory. No drop-ins will be loaded.");
            return new URL[0];
        }

        LOG.info("Drop-in directory is: "+dir);
        URL[] extJars = Files.list(dir)
                .filter(path -> path.getFileName().toString().endsWith(".jar"))
                .map(jar -> {
                    try {
                        return jar.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(URL[]::new);

        if (extJars.length > 0) {
            LOG.info("Adding the following external drop-in JAR files to class path: " + Arrays.toString(extJars));
        } else {
            LOG.warning("No drop-in JAR files found. No drop-ins will be loaded.");
        }

        return extJars;
    }
}
