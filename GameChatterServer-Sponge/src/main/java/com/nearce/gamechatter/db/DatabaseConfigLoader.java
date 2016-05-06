/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.db;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nearce.gamechatter.sponge.ChatCraftPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfigLoader {

    private Path getDatabaseFile() throws IOException {
        ConfigManager service = Sponge.getGame().getConfigManager();
        Path path = service.getPluginConfig(ChatCraftPlugin.inst()).getDirectory();
        return path.resolve("database.json");
    }

    public void init() {
        try {
            Class.forName("org.mariadb.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Insert ugly configuration code
        try {
            Path targetFile = getDatabaseFile();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            if (Files.exists(targetFile)) {
                try (BufferedReader reader = Files.newBufferedReader(targetFile)) {
                    DatabaseConfig config = gson.fromJson(reader, DatabaseConfig.class);

                    if (config == null || config.getDatabase().isEmpty()) {
                        return;
                    }

                    SQLHandle.setDatabase(config.getDatabase());
                    SQLHandle.setUsername(config.getUsername());
                    SQLHandle.setPassword(config.getPassword());
                }
            } else {
                Files.createFile(targetFile);
                try (BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
                    writer.write(gson.toJson(new DatabaseConfig()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
