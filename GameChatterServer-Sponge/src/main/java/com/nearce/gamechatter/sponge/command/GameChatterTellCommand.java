/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge.command;

import com.nearce.gamechatter.ChatMessage;
import com.nearce.gamechatter.ChatParticipant;
import com.nearce.gamechatter.RemoteChatParticipant;
import com.nearce.gamechatter.sponge.GameChatterPlugin;
import com.nearce.gamechatter.sponge.SpongeChatParticipant;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static org.spongepowered.api.command.args.GenericArguments.remainingJoinedStrings;
import static org.spongepowered.api.command.args.GenericArguments.seq;

public class GameChatterTellCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        ChatParticipant sourceParticipant;
        if (src instanceof Player) {
            sourceParticipant = new SpongeChatParticipant((Player) src);
        } else {
            sourceParticipant = new SpongeChatParticipant(src);
        }

        ChatMessage message = new ChatMessage(sourceParticipant, args.<String>getOne("message").get());
        String toName = args.<String>getOne("target").get();

        if (toName.equals(sourceParticipant.getName())) {
            src.sendMessage(Text.of(TextColors.RED, "You can't send a private message to yourself!"));
            return CommandResult.empty();
        }

        GameChatterPlugin.inst().getGameServer().remoteClientSendPrivateMessage(message, toName);
        GameChatterPlugin.inst().sendPrivateMessage(message, toName);

        return CommandResult.success();
    }

    private static class GameChatterMemberCommandElement extends PatternMatchingCommandElement {
        protected GameChatterMemberCommandElement(@Nullable Text key) {
            super(key);
        }

        private List<ChatParticipant> getAllParticipants() {
            List<ChatParticipant> players = GameChatterPlugin.inst().getGameServer().getLocalParticipants();
            Collection<RemoteChatParticipant> participants = GameChatterPlugin.inst().getConnectedParticipants();

            players.addAll(participants);

            return players;
        }

        @Override
        protected Iterable<String> getChoices(CommandSource source) {
            return getAllParticipants().stream().map(ChatParticipant::getName).collect(Collectors.toSet());
        }

        @Override
        protected String getValue(String choice) throws IllegalArgumentException {
            Optional<ChatParticipant> optParticipant = getAllParticipants().stream().filter(p -> p.getName().equalsIgnoreCase(choice)).findFirst();
            if (!optParticipant.isPresent()) {
                throw new IllegalArgumentException("Invalid input " + choice + " was found");
            }
            return optParticipant.get().getName();
        }
    }

    public static CommandSpec aquireSpec() {
        return CommandSpec.builder()
                .description(Text.of("Send private messages"))
                .arguments(
                        seq(
                                new GameChatterMemberCommandElement(Text.of("target")),
                                remainingJoinedStrings(Text.of("message"))
                        )
                ).executor(new GameChatterTellCommand()).build();
    }
}
