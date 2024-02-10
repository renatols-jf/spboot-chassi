package com.github.renatolsjf.chassi;

public class Chassi {

    private static Chassi instance = new Chassi();

    private Configuration config = new Configuration();

    private Chassi() {}

    public Configuration getConfig() {
        return this.config;
    }

    public void setConfig(Configuration config) {
        if (config != null) {
            this.config = config;
        }
    }

    public static Chassi getInstance() {
        return instance;
    }

}
