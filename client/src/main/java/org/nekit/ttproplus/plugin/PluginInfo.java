package org.nekit.ttproplus.plugin;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Metadata descriptor for a TeamTalk Next plugin.
 */
public class PluginInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();

    @SerializedName("id")
    private String id = "";

    @SerializedName("name")
    private String name = "";

    @SerializedName("version")
    private String version = "1.0.0";

    @SerializedName("versionCode")
    private int versionCode = 1;

    @SerializedName("author")
    private String author = "";

    @SerializedName("description")
    private String description = "";

    @SerializedName("mainClass")
    private String mainClass = "";

    @SerializedName("minClientVersion")
    private String minClientVersion = "5.28.6";

    public PluginInfo() {
    }

    public PluginInfo(String id, String name, String version, int versionCode, String author, String description, String mainClass) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.version = version != null ? version : "1.0.0";
        this.versionCode = versionCode;
        this.author = author != null ? author : "";
        this.description = description != null ? description : "";
        this.mainClass = mainClass != null ? mainClass : "";
    }

    public static PluginInfo fromJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return new PluginInfo();
        }
        PluginInfo info = GSON.fromJson(jsonStr, PluginInfo.class);
        if (info == null) {
            info = new PluginInfo();
        }
        return info;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return (name != null && !name.isEmpty()) ? name : id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMinClientVersion() {
        return minClientVersion;
    }

    public void setMinClientVersion(String minClientVersion) {
        this.minClientVersion = minClientVersion;
    }

    @Override
    public String toString() {
        return getName() + " v" + getVersion() + " (" + getId() + ")";
    }
}
