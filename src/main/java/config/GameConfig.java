/*
 * Copyright (c) 2025 Edward T. Tonai
 * Licensed under the MIT License - see LICENSE file for details
 */

package config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class GameConfig {
    
    @JsonProperty("display")
    private DisplayConfig display = new DisplayConfig();
    
    @JsonProperty("version")
    private String version = "1.0";
    
    @JsonProperty("configVersion")
    private int configVersion = 1;
    
    private static GameConfig instance;
    private static final String CONFIG_FILE_PATH = "/config/game-config.json";
    
    public static GameConfig getInstance() {
        if (instance == null) {
            instance = loadConfiguration();
        }
        return instance;
    }
    
    private static GameConfig loadConfiguration() {
        GameConfig config = new GameConfig();
        
        try {
            InputStream inputStream = GameConfig.class.getResourceAsStream(CONFIG_FILE_PATH);
            if (inputStream != null) {
                ObjectMapper mapper = new ObjectMapper();
                config = mapper.readValue(inputStream, GameConfig.class);
                System.out.println("Game configuration loaded successfully from " + CONFIG_FILE_PATH);
            } else {
                System.out.println("Configuration file not found at " + CONFIG_FILE_PATH + ", using defaults");
            }
        } catch (IOException e) {
            System.err.println("Error loading game configuration: " + e.getMessage());
            System.out.println("Using default configuration values");
        }
        
        return config;
    }
    
    public DisplayConfig getDisplay() {
        return display;
    }
    
    public void setDisplay(DisplayConfig display) {
        this.display = display;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public int getConfigVersion() {
        return configVersion;
    }
    
    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }
    
    public static class DisplayConfig {
        
        @JsonProperty("window")
        private WindowConfig window = new WindowConfig();
        
        public WindowConfig getWindow() {
            return window;
        }
        
        public void setWindow(WindowConfig window) {
            this.window = window;
        }
    }
    
    public static class WindowConfig {
        
        @JsonProperty("width")
        private int width = 800;
        
        @JsonProperty("height")
        private int height = 600;
        
        @JsonProperty("fullscreen")
        private boolean fullscreen = false;
        
        @JsonProperty("resizable")
        private boolean resizable = true;
        
        @JsonProperty("title")
        private String title = "OpenFields2";
        
        public int getWidth() {
            return width;
        }
        
        public void setWidth(int width) {
            this.width = width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setHeight(int height) {
            this.height = height;
        }
        
        public boolean isFullscreen() {
            return fullscreen;
        }
        
        public void setFullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
        }
        
        public boolean isResizable() {
            return resizable;
        }
        
        public void setResizable(boolean resizable) {
            this.resizable = resizable;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
    }
}