package io.github.renatolsjf.chassis.loader;

import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.env.EnvScalarConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Loader {

    private final static String LABELS_FILE = "chassis-labels";
    private final static String CONFIG_FILE = "chassis-config";
    private final static String API_FILE = "chassis-api";
    private final static String[] SUPPORTED_EXTENSIONS = new String[]{".yaml", ".yml"};

    private Map<String, Object> labelsData;
    private Map<String, Object> configData;
    private Map<String, Object> apiData;

    private Loader() {}

    public static Loader defaultLoader() {
        return new Loader().loadConfig().loadLabels().loadApi();
    }

    private Loader loadConfig() {
        Yaml yaml = new Yaml(new EnvScalarConstructor());
        yaml.addImplicitResolver(EnvScalarConstructor.ENV_TAG, EnvScalarConstructor.ENV_FORMAT, "$");
        for (String ext: SUPPORTED_EXTENSIONS) {
            try {
                ClassPathResource c = new ClassPathResource(CONFIG_FILE + ext);
                try (InputStream is = c.getInputStream()) {
                    this.configData = yaml.load(is);
                    return this;
                }
            } catch (IOException ex) {
                //TODO log issue
            }
        }
        return this;
    }

    private Loader loadLabels() {
        Yaml yaml = new Yaml();
        Locale l = Locale.getDefault();
        List<String> suffixes = Arrays.asList("_" + l.toString(), "_" + l.getLanguage() + "_" + l.getCountry(), "_" + l.getLanguage(), "");
        for (String suffix: suffixes) {
            for (String ext: SUPPORTED_EXTENSIONS) {
                try {
                    ClassPathResource c = new ClassPathResource(LABELS_FILE + suffix + ext);
                    try (InputStream is = c.getInputStream()) {
                        this.labelsData = yaml.load(is);
                        return this;
                    }
                } catch (IOException ex) {
                    //TODO log issue
                }
            }
        }
        return this;
    }

    private Loader loadApi() {
        Yaml yaml = new Yaml(new EnvScalarConstructor());
        yaml.addImplicitResolver(EnvScalarConstructor.ENV_TAG, EnvScalarConstructor.ENV_FORMAT, "$");
        for (String ext: SUPPORTED_EXTENSIONS) {
            try {
                ClassPathResource c = new ClassPathResource(API_FILE + ext);
                try (InputStream is = c.getInputStream()) {
                    this.apiData = yaml.load(is);
                    return this;
                }
            } catch (IOException ex) {
                //TODO log issue
            }
        }
        return this;
    }

    public Map<String, Object> getConfigData() {
        return this.configData;
    }

    public Map<String, Object> getLabelsData() {
        return this.labelsData;
    }

    public Map<String, Object> getApiData() {
        return this.apiData;
    }

}
