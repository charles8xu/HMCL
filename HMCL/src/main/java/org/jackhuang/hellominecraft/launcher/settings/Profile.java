/*
 * Copyright 2013 huangyuhui <huanghongxun2008@126.com>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.
 */
package org.jackhuang.hellominecraft.launcher.settings;

import java.io.File;
import org.jackhuang.hellominecraft.launcher.launch.IMinecraftProvider;
import org.jackhuang.hellominecraft.utils.system.IOUtils;
import org.jackhuang.hellominecraft.launcher.utils.MCUtils;
import org.jackhuang.hellominecraft.launcher.version.GameDirType;
import org.jackhuang.hellominecraft.utils.StrUtils;
import org.jackhuang.hellominecraft.utils.Utils;
import org.jackhuang.hellominecraft.launcher.version.MinecraftVersion;
import org.jackhuang.hellominecraft.launcher.version.MinecraftVersionManager;
import org.jackhuang.hellominecraft.utils.system.Java;
import org.jackhuang.hellominecraft.utils.system.OS;

/**
 *
 * @author huangyuhui
 */
public final class Profile {

    private String name, selectedMinecraftVersion = "", javaArgs, minecraftArgs, maxMemory, permSize, width, height, userProperties;
    private String gameDir, javaDir, wrapperLauncher, serverIp, java;
    private boolean fullscreen, debug, noJVMArgs, canceledWrapper;

    /**
     * 0 - Close the launcher when the game starts.<br/>
     * 1 - Hide the launcher when the game starts.<br/>
     * 2 - Keep the launcher open.<br/>
     */
    private int launcherVisibility;

    /**
     * 0 - .minecraft<br/>
     * 1 - .minecraft/versions/&lt;version&gt;/<br/>
     */
    private int gameDirType;

    protected transient IMinecraftProvider minecraftProvider;

    public Profile() {
        this("Default");
    }

    public Profile(String name) {
        this.name = name;
        gameDir = MCUtils.getInitGameDir().getPath();
        debug = fullscreen = canceledWrapper = false;
        javaDir = "";
        java = "Default";
        launcherVisibility = gameDirType = 0;
        minecraftArgs = serverIp = "";
    }

    public Profile(Profile v) {
        this();
        if (v == null) return;
        name = v.name;
        gameDir = v.gameDir;
        maxMemory = v.maxMemory;
        width = v.width;
        height = v.height;
        java = v.java;
        fullscreen = v.fullscreen;
        javaArgs = v.javaArgs;
        javaDir = v.javaDir;
        debug = v.debug;
        minecraftArgs = v.minecraftArgs;
        permSize = v.permSize;
        gameDirType = v.gameDirType;
        canceledWrapper = v.canceledWrapper;
        noJVMArgs = v.noJVMArgs;
        launcherVisibility = v.launcherVisibility;
        wrapperLauncher = v.wrapperLauncher;
        serverIp = v.serverIp;
    }

    public IMinecraftProvider getMinecraftProvider() {
        if (minecraftProvider == null) minecraftProvider = new MinecraftVersionManager(this);
        return minecraftProvider;
    }

    public MinecraftVersion getSelectedMinecraftVersion() {
        if (StrUtils.isBlank(selectedMinecraftVersion)) {
            MinecraftVersion v = getMinecraftProvider().getOneVersion();
            if (v == null) return null;
            selectedMinecraftVersion = v.id;
            return v;
        }
        MinecraftVersion v = getMinecraftProvider().getVersionById(selectedMinecraftVersion);
        if (v == null) v = getMinecraftProvider().getOneVersion();
        if (v != null) setSelectedMinecraftVersion(v.id);
        return v;
    }

    public String getGameDir() {
        if (StrUtils.isBlank(gameDir)) gameDir = MCUtils.getInitGameDir().getPath();
        return IOUtils.addSeparator(gameDir);
    }

    public String getCanonicalGameDir() {
        return IOUtils.tryGetCanonicalFolderPath(getGameDirFile());
    }

    public File getCanonicalGameDirFile() {
        return IOUtils.tryGetCanonicalFile(getGameDirFile());
    }

    public File getGameDirFile() {
        return new File(getGameDir());
    }

    public Profile setGameDir(String gameDir) {
        this.gameDir = gameDir;
        Settings.save();
        return this;
    }

    public String getJavaDir() {
        Java j = getJava();
        if (j.getHome() == null) return javaDir;
        else return j.getJava();
    }

    public String getSettingsJavaDir() {
        return javaDir;
    }

    public File getJavaDirFile() {
        return new File(getJavaDir());
    }

    public void setJavaDir(String javaDir) {
        this.javaDir = javaDir;
        Settings.save();
    }

    public Java getJava() {
        return Settings.JAVA.get(getJavaIndexInAllJavas());
    }

    public int getJavaIndexInAllJavas() {
        if(StrUtils.isBlank(java) && StrUtils.isNotBlank(javaDir)) {
            java = "Custom";
        }
        int idx = Settings.JAVA.indexOf(new Java(java, null));
        if (idx == -1) {
            java = "Default";
            idx = 0;
        }
        return idx;
    }

    public void setJava(Java java) {
        int idx = Settings.JAVA.indexOf(java);
        if (idx == -1) return;
        this.java = java.getName();
        Settings.save();
    }

    public File getFolder(String folder) {
        return new File(getGameDir(), folder);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSelectedMinecraftVersion(String selectedMinecraftVersion) {
        this.selectedMinecraftVersion = selectedMinecraftVersion;
    }

    public String getJavaArgs() {
        if (StrUtils.isBlank(javaArgs)) return "";
        return javaArgs;
    }

    public void setJavaArgs(String javaArgs) {
        this.javaArgs = javaArgs;
        Settings.save();
    }

    public boolean hasJavaArgs() {
        return StrUtils.isNotBlank(getJavaArgs().trim());
    }

    public String getMaxMemory() {
        if (StrUtils.isBlank(maxMemory)) return String.valueOf(Utils.getSuggestedMemorySize());
        return maxMemory;
    }

    public void setMaxMemory(String maxMemory) {
        this.maxMemory = maxMemory;
        Settings.save();
    }

    public String getWidth() {
        if (StrUtils.isBlank(width)) return "854";
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        if (StrUtils.isBlank(height)) return "480";
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        Settings.save();
    }

    public String getUserProperties() {
        if (userProperties == null) return "";
        return userProperties;
    }

    public void setUserProperties(String userProperties) {
        this.userProperties = userProperties;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        Settings.save();
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        Settings.save();
    }

    public LauncherVisibility getLauncherVisibility() {
        return LauncherVisibility.values()[launcherVisibility];
    }

    public void setLauncherVisibility(LauncherVisibility launcherVisibility) {
        this.launcherVisibility = launcherVisibility.ordinal();
        Settings.save();
    }

    public GameDirType getGameDirType() {
        return GameDirType.values()[gameDirType];
    }

    public void setGameDirType(GameDirType gameDirType) {
        this.gameDirType = gameDirType.ordinal();
        Settings.save();
    }

    public String getPermSize() {
        return permSize;
    }

    public void setPermSize(String permSize) {
        this.permSize = permSize;
        Settings.save();
    }

    public boolean isNoJVMArgs() {
        return noJVMArgs;
    }

    public void setNoJVMArgs(boolean noJVMArgs) {
        this.noJVMArgs = noJVMArgs;
    }

    public String getMinecraftArgs() {
        return minecraftArgs;
    }

    public void setMinecraftArgs(String minecraftArgs) {
        this.minecraftArgs = minecraftArgs;
        Settings.save();
    }

    public boolean isCanceledWrapper() {
        return canceledWrapper;
    }

    public void setCanceledWrapper(boolean canceledWrapper) {
        this.canceledWrapper = canceledWrapper;
        Settings.save();
    }

    public String getWrapperLauncher() {
        return wrapperLauncher;
    }

    public void setWrapperLauncher(String wrapperLauncher) {
        this.wrapperLauncher = wrapperLauncher;
        Settings.save();
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
        Settings.save();
    }

    public void checkFormat() {
        gameDir = gameDir.replace('/', OS.os().fileSeparator).replace('\\', OS.os().fileSeparator);
    }
}
