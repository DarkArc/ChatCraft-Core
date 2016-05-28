/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge;

import com.nearce.gamechatter.ChatParticipant;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.UUID;

public class SpongeChatUser {
    private final ChatParticipant participant;
    private final MessageChannel channel;

    public SpongeChatUser(Player player) {
        participant = new ChatParticipant(player.getName());
        this.channel = MessageChannel.fixed(player);
    }

    public SpongeChatUser(CommandSource abstractSource) {
        participant = new ChatParticipant(abstractSource.getName());
        this.channel = MessageChannel.fixed(abstractSource);
    }

    public ChatParticipant getParticipant() {
        return participant;
    }

    public MessageChannel getChannel() {
        return channel;
    }
}
