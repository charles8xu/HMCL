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
package org.jackhuang.hellominecraft.svrmgr.settings;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;
import org.jackhuang.hellominecraft.HMCLog;
import org.jackhuang.hellominecraft.utils.system.FileUtils;
import org.jackhuang.hellominecraft.utils.StrUtils;
import org.jackhuang.hellominecraft.svrmgr.settings.PlayerList.BasePlayer;

/**
 *
 * @author huangyuhui
 * @param <T> Player type.
 */
public abstract class PlayerList<T extends BasePlayer> {

    public static class BasePlayer {

        public String uuid, name;

        public BasePlayer(String name) {
            uuid = UUID.randomUUID().toString();
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PlayerList.BasePlayer) {
                BasePlayer player = (BasePlayer) obj;
                return player.name.equals(name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public HashSet<T> op;

    protected abstract T newPlayer(String name);

    public void initByText(String s) {
        String[] lines = s.split("\n");
        op = new HashSet<T>();
        for (String l : lines) {
            if (l.startsWith("#")) continue;
            T player = newPlayer(l);
            if (StrUtils.isBlank(l))
                continue;
            op.add(player);
        }
    }

    public void initByJson(String s) {
        op = new Gson().<HashSet<T>>fromJson(s, HashSet.class);
    }

    public void initByBoth(File txt, File json) {
        HashSet<T> player = new HashSet<T>();
        /*op = null;
         if(json.exists()) {
         try {
         initByJson(FileUtils.readFileToStringIgnoreFileNotFound(json));
         if(op != null)
         player.addAll(op);
         } catch(IOException e) {
         HMCLLog.warn("Failed to load playerlist by json", e);
         }
         }*/
        op = null;
        if (txt.exists())
            try {
                initByText(FileUtils.readFileToStringIgnoreFileNotFound(txt));
                if (op != null)
                    player.addAll(op);
            } catch (IOException e) {
                HMCLog.warn("Failed to load playerlist by txt", e);
            }
        op = player;
    }

    public void saveAsText(File file) throws IOException {
        FileUtils.write(file, StrUtils.parseParams("", op, System.getProperty("line.separator")));
    }

    public void saveAsJson(File file) throws IOException {
        FileUtils.write(file, new Gson().toJson(op));
    }

    public void saveAsBoth(File txt, File json) throws IOException {
        saveAsText(txt);
        saveAsJson(json);
    }
}
