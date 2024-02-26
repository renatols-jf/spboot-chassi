package io.github.renatolsjf.chassis.loader;

import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Loader {

    private final static String LABELS_FILE = "chassis-labels";
    private final static String[] SUPPORTED_EXTENSIONS = new String[]{".yaml", ".yml"};

    private Map<String, Object> labelsData;

    private Loader() {}

    public static Loader defaultLoader() {
        return new Loader().load();
    }

    private Loader load() {
        Yaml yaml = new Yaml();
        //InputStream is = null;
        for (String ext: SUPPORTED_EXTENSIONS) {
            try {
                File f = ResourceUtils.getFile("classpath:" + LABELS_FILE + ext);
                if (f != null) {
                    try (InputStream is = new FileInputStream(f)) {
                        this.labelsData = yaml.load(is);
                        break;
                    }
                }
            } catch (IOException ex) {
                //TODO log issue
            }
        }
        return this;
    }

    public Map<String, Object> getLabelsData() {
        return this.labelsData;
    }

}
