package net.ddellspe.twitchstreamassistant.model;

import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class TwitchChatMessage {
  private @NonNull Map<String, String> tags;
  private @Nullable List<String> badges;
  private @Nullable List<EmoteLocation> emotes;
  private @NonNull Set<String> permissions;
  private @NonNull String username;
  private @NonNull String command;
  private @Nullable String payload;
  private @NonNull String message;
  private @NonNull String rawMessage;
  private boolean actionMessage;

  public TwitchChatMessage(ChannelMessageEvent event) {
    IRCMessageEvent ircMessageEvent = event.getMessageEvent();
    this.tags = ircMessageEvent.getTags();
    this.badges =
        ircMessageEvent.getTags().get("badges") == null
            ? null
            : Arrays.stream(ircMessageEvent.getTags().get("badges").split(","))
                .collect(Collectors.toList());
    this.emotes =
        ircMessageEvent.getTags().get("emotes") == null
            ? null
            : EmoteLocation.parse(ircMessageEvent.getTags().get("emotes"));
    this.permissions =
        ircMessageEvent.getClientPermissions().stream()
            .map(CommandPermission::toString)
            .collect(Collectors.toSet());
    this.username = ircMessageEvent.getUserName();
    this.command = ircMessageEvent.getCommandType();
    this.payload =
        ircMessageEvent.getPayload().isPresent() ? ircMessageEvent.getPayload().get() : null;
    this.message = event.getMessage();
    this.rawMessage = ircMessageEvent.getRawMessage();
    this.actionMessage = false;
  }

  public TwitchChatMessage(ChannelMessageActionEvent event) {
    IRCMessageEvent ircMessageEvent = event.getMessageEvent();
    this.tags = ircMessageEvent.getTags();
    this.badges =
        ircMessageEvent.getTags().get("badges") == null
            ? null
            : Arrays.stream(ircMessageEvent.getTags().get("badges").split(","))
                .collect(Collectors.toList());
    this.emotes =
        ircMessageEvent.getTags().get("emotes") == null
            ? null
            : EmoteLocation.parse(ircMessageEvent.getTags().get("emotes"));
    this.permissions =
        ircMessageEvent.getClientPermissions().stream()
            .map(CommandPermission::toString)
            .collect(Collectors.toSet());
    this.username = ircMessageEvent.getUserName();
    this.command = ircMessageEvent.getCommandType();
    this.payload =
        ircMessageEvent.getPayload().isPresent() ? ircMessageEvent.getPayload().get() : null;
    this.message = event.getMessage();
    this.rawMessage = ircMessageEvent.getRawMessage();
    this.actionMessage = true;
  }

  public static class EmoteLocation implements Comparable<EmoteLocation> {
    private int startIndex;
    private int endIndex;
    private @NonNull String emoteId;

    public EmoteLocation(int startIndex, int endIndex, @NonNull String emoteId) {
      this.startIndex = startIndex;
      this.endIndex = endIndex;
      this.emoteId = emoteId;
    }

    public static @NonNull List<EmoteLocation> parse(@NonNull String input) {
      List<EmoteLocation> emoteLocations = new ArrayList<>();
      for (String singleEmote : input.split("/")) {
        String emoteId = singleEmote.split(":")[0];
        for (String position : singleEmote.split(":")[1].split(",")) {
          emoteLocations.add(
              new EmoteLocation(
                  Integer.parseInt(position.split("-")[0]),
                  Integer.parseInt(position.split("-")[1]),
                  emoteId));
        }
      }
      Collections.sort(emoteLocations);
      return emoteLocations;
    }

    public int getStartIndex() {
      return startIndex;
    }

    public void setStartIndex(int startIndex) {
      this.startIndex = startIndex;
    }

    public int getEndIndex() {
      return endIndex;
    }

    public void setEndIndex(int endIndex) {
      this.endIndex = endIndex;
    }

    public @NonNull String getEmoteId() {
      return emoteId;
    }

    public void setEmoteId(@NonNull String emoteId) {
      this.emoteId = Objects.requireNonNull(emoteId);
    }

    @Override
    public int compareTo(@NonNull TwitchChatMessage.EmoteLocation o) {
      return startIndex - o.startIndex;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      EmoteLocation that = (EmoteLocation) o;
      return startIndex == that.startIndex
          && endIndex == that.endIndex
          && emoteId.equals(that.emoteId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(startIndex, endIndex, emoteId);
    }

    @Override
    public String toString() {
      return "EmoteLocations{"
          + "startIndex="
          + startIndex
          + ", endIndex="
          + endIndex
          + ", emoteId='"
          + emoteId
          + '\''
          + '}';
    }
  }

  public @NonNull Map<String, String> getTags() {
    return tags;
  }

  public void setTags(@NonNull Map<String, String> tags) {
    this.tags = Objects.requireNonNull(tags);
  }

  public @Nullable List<String> getBadges() {
    return badges;
  }

  public void setBadges(@Nullable List<String> badges) {
    this.badges = badges;
  }

  public @Nullable List<EmoteLocation> getEmotes() {
    return emotes;
  }

  public void setEmotes(@Nullable String emotes) {
    this.emotes = EmoteLocation.parse(emotes);
  }

  public @NonNull Set<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(@NonNull Set<CommandPermission> permissions) {
    this.permissions =
        Objects.requireNonNull(
            permissions.stream().map(CommandPermission::toString).collect(Collectors.toSet()));
  }

  public @NonNull String getUsername() {
    return username;
  }

  public void setUsername(@NonNull String username) {
    this.username = Objects.requireNonNull(username);
  }

  public @NonNull String getCommand() {
    return command;
  }

  public void setCommand(@NonNull String command) {
    this.command = Objects.requireNonNull(command);
  }

  public @Nullable String getPayload() {
    return payload;
  }

  public void setPayload(@Nullable String payload) {
    this.payload = Objects.requireNonNull(payload);
  }

  public @NonNull String getMessage() {
    return message;
  }

  public void setMessage(@NonNull String message) {
    this.message = Objects.requireNonNull(message);
  }

  public @NonNull String getRawMessage() {
    return rawMessage;
  }

  public void setRawMessage(@NonNull String rawMessage) {
    this.rawMessage = rawMessage;
  }

  public boolean isActionMessage() {
    return actionMessage;
  }

  public void setActionMessage(boolean actionMessage) {
    this.actionMessage = actionMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TwitchChatMessage that = (TwitchChatMessage) o;
    return actionMessage == that.actionMessage
        && tags.equals(that.tags)
        && Objects.equals(badges, that.badges)
        && Objects.equals(emotes, that.emotes)
        && permissions.equals(that.permissions)
        && username.equals(that.username)
        && command.equals(that.command)
        && Objects.equals(payload, that.payload)
        && message.equals(that.message)
        && rawMessage.equals(that.rawMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        tags,
        badges,
        emotes,
        permissions,
        username,
        command,
        payload,
        message,
        rawMessage,
        actionMessage);
  }

  @Override
  public String toString() {
    return "TwitchChatMessage{"
        + "tags="
        + tags
        + ", badges="
        + badges
        + ", emotes="
        + emotes
        + ", permissions="
        + permissions
        + ", username='"
        + username
        + '\''
        + ", command='"
        + command
        + '\''
        + ", payload='"
        + payload
        + '\''
        + ", message='"
        + message
        + '\''
        + ", rawMessage='"
        + rawMessage
        + '\''
        + ", actionMessage="
        + actionMessage
        + '}';
  }
}
