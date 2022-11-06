package cn.edu.nju.zxz.simplerpc.config;

import java.io.IOException;
import java.util.Properties;

public class SimpleRPCConfig {
    private static final Properties PROPERTIES;

    private SimpleRPCConfig() {}

    static {
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(SimpleRPCConfig.class.getResourceAsStream("/simple-rpc-config.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file [simple-rpc-config.properties].", e);
        }
    }

    public static String getConfig(String name) {
        return PROPERTIES.getProperty(name);
    }
}
