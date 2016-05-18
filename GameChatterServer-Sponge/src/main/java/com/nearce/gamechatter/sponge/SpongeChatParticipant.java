/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge;

import com.nearce.gamechatter.ChatParticipant;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.UUID;

public class SpongeChatParticipant extends ChatParticipant {
    private MessageChannel channel;

    public SpongeChatParticipant(Player player, UUID identifier, String name) {
        super(identifier, name);
        this.channel = MessageChannel.fixed(player);
    }

    @Override
    public void sendMessage(String message) {
        channel.send(Text.of(message));
    }
}
