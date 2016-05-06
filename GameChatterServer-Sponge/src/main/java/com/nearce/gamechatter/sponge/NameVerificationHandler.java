/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge;

import com.nearce.gamechatter.db.SQLHandle;
import org.jooq.impl.DSL;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.nearce.gamechatter.db.schema.tables.VerifiedUsers.VERIFIED_USERS;

public class NameVerificationHandler implements Runnable {
    private Map<String, NameVerificationSession> codeMap = new HashMap<>();

    public NameVerificationHandler() {
        Task.builder().execute(this).interval(5, TimeUnit.MINUTES).submit(GameChatterPlugin.inst());
    }

    public Optional<String> getVerifiedName(String name, UUID clientID) {
        Optional<User> optUser = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(name);
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (checkDatabase(user.getUniqueId(), clientID)) {
                return Optional.of(user.getName());
            }
        }
        return Optional.empty();
    }

    public void registerAttempt(String code, String name, UUID clientID, Consumer<String> join) {
        codeMap.put(code, new NameVerificationSession(name, clientID, join));
    }

    public boolean tryAttempt(String code, User user) {
        NameVerificationSession session = codeMap.remove(code);

        if (session != null && session.getName().equalsIgnoreCase(user.getName())) {
            addDatabase(user.getUniqueId(), session.getClientID());
            session.getJoin().accept(user.getName());
            return true;
        }
        return false;
    }

    private boolean checkDatabase(UUID minecraftUUID, UUID clientUUID) {
        try (Connection con = SQLHandle.getConnection()) {
            return !DSL.using(con).selectOne().from(VERIFIED_USERS).where(
                    VERIFIED_USERS.MC_UUID.equal(minecraftUUID.toString()).and(VERIFIED_USERS.CLIENT_UUID.equal(clientUUID.toString()))
            ).fetch().isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addDatabase(UUID minecraftUUID, UUID clientUUID) {
        try (Connection con = SQLHandle.getConnection()) {
            DSL.using(con).insertInto(VERIFIED_USERS).columns(
                    VERIFIED_USERS.MC_UUID, VERIFIED_USERS.CLIENT_UUID
            ).values(
                    minecraftUUID.toString(), clientUUID.toString()
            ).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Iterator<Map.Entry<String, NameVerificationSession>> it = codeMap.entrySet().iterator();
        while (it.hasNext()) {
            if (System.currentTimeMillis() - it.next().getValue().getCreationTime() > TimeUnit.MINUTES.toMillis(5)) {
                it.remove();
            }
        }
    }
}
