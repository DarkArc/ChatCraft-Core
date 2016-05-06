/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge;

import com.nearce.gamechatter.ChatMessage;
import com.nearce.gamechatter.ChatParticipant;
import com.nearce.gamechatter.GameServer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SpongeGameServer implements GameServer {

    private NameVerificationHandler nameVerificationHandler = new NameVerificationHandler();
    private List<ChatParticipant> participants = new CopyOnWriteArrayList<>();

    public NameVerificationHandler getNameVerificationHandler() {
        return nameVerificationHandler;
    }

    @Override
    public List<ChatParticipant> getLocalParticipants() {
        return participants;
    }

    @Override
    public String sanitize(String input) {
        return input.replaceAll("[^\\x00-\\x80]", "");
    }

    @Override
    public void joinWhenLegal(String name, UUID clientID, Consumer<String> verification, Consumer<String> join) {
        Optional<String> optName = nameVerificationHandler.getVerifiedName(name, clientID);
        if (optName.isPresent()) {
            join.accept(optName.get());
            return;
        }

        String verificationCode = VerificationCodeGenerator.generate();
        nameVerificationHandler.registerAttempt(verificationCode, name, clientID, join);
        verification.accept(verificationCode);
    }

    @Override
    public void remoteClientJoin(ChatParticipant client) {
        MessageChannel.TO_ALL.send(Text.of(TextColors.YELLOW, client.getName(), " has joined remote chat"));
    }

    @Override
    public void remoteClientLeave(ChatParticipant client) {
        MessageChannel.TO_ALL.send(Text.of(TextColors.YELLOW, client.getName(), " has left remote chat"));
    }

    @Override
    public void remoteClientSendMessage(ChatMessage message) {
        MessageChannel.TO_ALL.send(Text.of("<", message.getSender().getName(), "> ", message.getMessage()));
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new ChatParticipant(player.getUniqueId(), player.getName());
        participants.add(participant);
    }

    @Listener(order = Order.POST)
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
        Player player = event.getTargetEntity();
        ChatParticipant participant = new ChatParticipant(player.getUniqueId(), player.getName());
        participants.remove(participant);
    }
}
