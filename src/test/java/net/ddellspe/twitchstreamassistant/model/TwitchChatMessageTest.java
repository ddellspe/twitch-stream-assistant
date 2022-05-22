package net.ddellspe.twitchstreamassistant.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TwitchChatMessageTest {

  @Test
  public void testEmoteParsingWithMultipleLocationsForASingleEmote() {
    List<TwitchChatMessage.EmoteLocation> expectedLocations =
        List.of(
            new TwitchChatMessage.EmoteLocation(
                10, 17, "emotesv2_b82879c544754d0793fa79de06c10f40"),
            new TwitchChatMessage.EmoteLocation(5, 8, "emotesv2_6f762cfbb37a4a1db4f31c21599947ee"),
            new TwitchChatMessage.EmoteLocation(0, 3, "emotesv2_6f762cfbb37a4a1db4f31c21599947ee"));
    List<TwitchChatMessage.EmoteLocation> actualLocations =
        TwitchChatMessage.EmoteLocation.parse(
            "emotesv2_6f762cfbb37a4a1db4f31c21599947ee:0-3,5-8/emotesv2_b82879c544754d0793fa79de06c10f40:10-17");
    assertEquals(expectedLocations, actualLocations);
    assertEquals(expectedLocations.hashCode(), actualLocations.hashCode());
  }

  @Test
  public void testEmoteParsingOnlyOneEmote() {
    List<TwitchChatMessage.EmoteLocation> expectedLocations =
        List.of(
            new TwitchChatMessage.EmoteLocation(0, 3, "emotesv2_6f762cfbb37a4a1db4f31c21599947ee"));
    List<TwitchChatMessage.EmoteLocation> actualLocations =
        TwitchChatMessage.EmoteLocation.parse("emotesv2_6f762cfbb37a4a1db4f31c21599947ee:0-3");
    assertEquals(expectedLocations, actualLocations);
    assertEquals(expectedLocations.hashCode(), actualLocations.hashCode());
  }

  @Test
  public void testEmoteLocationToString() {
    TwitchChatMessage.EmoteLocation location =
        new TwitchChatMessage.EmoteLocation(0, 3, "emotesv2_6f762cfbb37a4a1db4f31c21599947ee");
    assertEquals(0, location.getStartIndex());
    assertEquals(3, location.getEndIndex());
    assertEquals("emotesv2_6f762cfbb37a4a1db4f31c21599947ee", location.getEmoteId());
    assertEquals(
        "EmoteLocations{startIndex=0, endIndex=3, emoteId='emotesv2_6f762cfbb37a4a1db4f31c21599947ee'}",
        location.toString());
    location.setStartIndex(1);
    location.setEndIndex(2);
    location.setEmoteId("blah");
    TwitchChatMessage.EmoteLocation updatedLocation =
        new TwitchChatMessage.EmoteLocation(1, 2, "blah");
    assertEquals(updatedLocation, location);
  }

  @Test
  public void testChannelMessageEventEverythingAvailableConstructor() {
    ChannelMessageEvent mockMessage = Mockito.mock(ChannelMessageEvent.class);
    IRCMessageEvent mockIRCMessage = Mockito.mock(IRCMessageEvent.class);
    when(mockMessage.getMessageEvent()).thenReturn(mockIRCMessage);
    when(mockMessage.getMessage()).thenReturn("message");
    when(mockIRCMessage.getTags())
        .thenReturn(Map.of("badges", "broadcaster/1", "emotes", "123456:0-3"));
    when(mockIRCMessage.getClientPermissions()).thenReturn(Set.of(CommandPermission.BROADCASTER));
    when(mockIRCMessage.getUserName()).thenReturn("ddellspe");
    when(mockIRCMessage.getPayload()).thenReturn(Optional.of("payload"));
    when(mockIRCMessage.getRawMessage()).thenReturn("rawMessage");
    when(mockIRCMessage.getCommandType()).thenReturn("PRIVMSG");

    TwitchChatMessage actualMessage = new TwitchChatMessage(mockMessage);

    assertEquals("message", actualMessage.getMessage());
    assertEquals("rawMessage", actualMessage.getRawMessage());
    assertEquals(Set.of("BROADCASTER"), actualMessage.getPermissions());
    assertEquals("payload", actualMessage.getPayload());
    assertEquals(List.of("broadcaster/1"), actualMessage.getBadges());
    assertEquals(
        List.of(new TwitchChatMessage.EmoteLocation(0, 3, "123456")), actualMessage.getEmotes());
    assertEquals(
        Map.of("badges", "broadcaster/1", "emotes", "123456:0-3"), actualMessage.getTags());
    assertEquals("ddellspe", actualMessage.getUsername());
    assertEquals("PRIVMSG", actualMessage.getCommand());
    assertFalse(actualMessage.isActionMessage());

    TwitchChatMessage actualMessage2 = new TwitchChatMessage(mockMessage);
    assertEquals(actualMessage, actualMessage2);
    assertEquals(actualMessage.hashCode(), actualMessage2.hashCode());
  }

  @Test
  public void testChannelMessageEventNullBadgesAndEmotesNoPayloadConstructor() {
    ChannelMessageEvent mockMessage = Mockito.mock(ChannelMessageEvent.class);
    IRCMessageEvent mockIRCMessage = Mockito.mock(IRCMessageEvent.class);
    when(mockMessage.getMessageEvent()).thenReturn(mockIRCMessage);
    when(mockMessage.getMessage()).thenReturn("message");
    Map<String, String> actualTags =
        new HashMap<>() {
          {
            put("badges", null);
            put("emotes", null);
          }
        };
    when(mockIRCMessage.getTags()).thenReturn(actualTags);
    when(mockIRCMessage.getClientPermissions()).thenReturn(Set.of(CommandPermission.BROADCASTER));
    when(mockIRCMessage.getUserName()).thenReturn("ddellspe");
    when(mockIRCMessage.getPayload()).thenReturn(Optional.empty());
    when(mockIRCMessage.getRawMessage()).thenReturn("rawMessage");
    when(mockIRCMessage.getCommandType()).thenReturn("PRIVMSG");

    TwitchChatMessage actualMessage = new TwitchChatMessage(mockMessage);

    assertEquals("message", actualMessage.getMessage());
    assertEquals("rawMessage", actualMessage.getRawMessage());
    assertEquals(Set.of("BROADCASTER"), actualMessage.getPermissions());
    assertNull(actualMessage.getPayload());
    assertNull(actualMessage.getBadges());
    assertNull(actualMessage.getEmotes());
    assertEquals(actualTags, actualMessage.getTags());
    assertEquals("ddellspe", actualMessage.getUsername());
    assertEquals("PRIVMSG", actualMessage.getCommand());
    assertFalse(actualMessage.isActionMessage());

    TwitchChatMessage actualMessage2 = new TwitchChatMessage(mockMessage);
    assertEquals(actualMessage, actualMessage2);
    assertEquals(actualMessage.hashCode(), actualMessage2.hashCode());
  }

  @Test
  public void testChannelMessageActionEventEverythingAvailableConstructor() {
    ChannelMessageActionEvent mockMessage = Mockito.mock(ChannelMessageActionEvent.class);
    IRCMessageEvent mockIRCMessage = Mockito.mock(IRCMessageEvent.class);
    when(mockMessage.getMessageEvent()).thenReturn(mockIRCMessage);
    when(mockMessage.getMessage()).thenReturn("message");
    when(mockIRCMessage.getTags())
        .thenReturn(Map.of("badges", "broadcaster/1", "emotes", "123456:0-3"));
    when(mockIRCMessage.getClientPermissions()).thenReturn(Set.of(CommandPermission.BROADCASTER));
    when(mockIRCMessage.getUserName()).thenReturn("ddellspe");
    when(mockIRCMessage.getPayload()).thenReturn(Optional.of("payload"));
    when(mockIRCMessage.getRawMessage()).thenReturn("rawMessage");
    when(mockIRCMessage.getCommandType()).thenReturn("ACTION");

    TwitchChatMessage actualMessage = new TwitchChatMessage(mockMessage);

    assertEquals("message", actualMessage.getMessage());
    assertEquals("rawMessage", actualMessage.getRawMessage());
    assertEquals(Set.of("BROADCASTER"), actualMessage.getPermissions());
    assertEquals("payload", actualMessage.getPayload());
    assertEquals(List.of("broadcaster/1"), actualMessage.getBadges());
    assertEquals(
        List.of(new TwitchChatMessage.EmoteLocation(0, 3, "123456")), actualMessage.getEmotes());
    assertEquals(
        Map.of("badges", "broadcaster/1", "emotes", "123456:0-3"), actualMessage.getTags());
    assertEquals("ddellspe", actualMessage.getUsername());
    assertEquals("ACTION", actualMessage.getCommand());
    assertTrue(actualMessage.isActionMessage());

    TwitchChatMessage actualMessage2 = new TwitchChatMessage(mockMessage);
    assertEquals(actualMessage, actualMessage2);
    assertEquals(actualMessage.hashCode(), actualMessage2.hashCode());
  }

  @Test
  public void testChannelMessageActionEventNullBadgesAndEmotesNoPayloadConstructor() {
    ChannelMessageActionEvent mockMessage = Mockito.mock(ChannelMessageActionEvent.class);
    IRCMessageEvent mockIRCMessage = Mockito.mock(IRCMessageEvent.class);
    when(mockMessage.getMessageEvent()).thenReturn(mockIRCMessage);
    when(mockMessage.getMessage()).thenReturn("message");
    Map<String, String> actualTags =
        new HashMap<>() {
          {
            put("badges", null);
            put("emotes", null);
          }
        };
    when(mockIRCMessage.getTags()).thenReturn(actualTags);
    when(mockIRCMessage.getClientPermissions()).thenReturn(Set.of(CommandPermission.BROADCASTER));
    when(mockIRCMessage.getUserName()).thenReturn("ddellspe");
    when(mockIRCMessage.getPayload()).thenReturn(Optional.empty());
    when(mockIRCMessage.getRawMessage()).thenReturn("rawMessage");
    when(mockIRCMessage.getCommandType()).thenReturn("ACTION");

    TwitchChatMessage actualMessage = new TwitchChatMessage(mockMessage);

    assertEquals("message", actualMessage.getMessage());
    assertEquals("rawMessage", actualMessage.getRawMessage());
    assertEquals(Set.of("BROADCASTER"), actualMessage.getPermissions());
    assertNull(actualMessage.getPayload());
    assertNull(actualMessage.getBadges());
    assertNull(actualMessage.getEmotes());
    assertEquals(actualTags, actualMessage.getTags());
    assertEquals("ddellspe", actualMessage.getUsername());
    assertEquals("ACTION", actualMessage.getCommand());
    assertTrue(actualMessage.isActionMessage());

    TwitchChatMessage actualMessage2 = new TwitchChatMessage(mockMessage);
    assertEquals(actualMessage, actualMessage2);
    assertEquals(actualMessage.hashCode(), actualMessage2.hashCode());
  }

  @Test
  public void testTwitchChatMessageToStringSettersAndGetters() {
    ChannelMessageEvent mockMessage = Mockito.mock(ChannelMessageEvent.class);
    IRCMessageEvent mockIRCMessage = Mockito.mock(IRCMessageEvent.class);
    when(mockMessage.getMessageEvent()).thenReturn(mockIRCMessage);
    when(mockMessage.getMessage()).thenReturn("message");
    when(mockIRCMessage.getTags())
        .thenReturn(Map.of("badges", "broadcaster/1", "emotes", "123456:0-3"));
    when(mockIRCMessage.getClientPermissions()).thenReturn(Set.of(CommandPermission.BROADCASTER));
    when(mockIRCMessage.getUserName()).thenReturn("ddellspe");
    when(mockIRCMessage.getPayload()).thenReturn(Optional.of("payload"));
    when(mockIRCMessage.getRawMessage()).thenReturn("rawMessage");
    when(mockIRCMessage.getCommandType()).thenReturn("PRIVMSG");

    TwitchChatMessage actualMessage = new TwitchChatMessage(mockMessage);

    assertEquals("message", actualMessage.getMessage());
    assertEquals("rawMessage", actualMessage.getRawMessage());
    assertEquals(Set.of("BROADCASTER"), actualMessage.getPermissions());
    assertEquals("payload", actualMessage.getPayload());
    assertEquals(List.of("broadcaster/1"), actualMessage.getBadges());
    assertEquals(
        List.of(new TwitchChatMessage.EmoteLocation(0, 3, "123456")), actualMessage.getEmotes());
    assertEquals(
        Map.of("badges", "broadcaster/1", "emotes", "123456:0-3"), actualMessage.getTags());
    assertEquals("ddellspe", actualMessage.getUsername());
    assertEquals("PRIVMSG", actualMessage.getCommand());
    assertFalse(actualMessage.isActionMessage());

    // we need the tags to be in a specific order for the toString call, hence the LinkedHashMap
    // being used
    Map<String, String> tags =
        new LinkedHashMap<>() {
          {
            put("badges", "premium/1");
            put("emotes", "234567:1-4");
          }
        };
    actualMessage.setMessage("message2");
    actualMessage.setRawMessage("rawMessage2");
    actualMessage.setPermissions(Set.of(CommandPermission.EVERYONE));
    actualMessage.setPayload("payload2");
    actualMessage.setBadges(List.of("premium/1"));
    actualMessage.setEmotes("234567:1-4");
    actualMessage.setTags(tags);
    actualMessage.setUsername("ddellspebot");
    actualMessage.setCommand("ACTION");
    actualMessage.setActionMessage(true);

    assertEquals(
        "TwitchChatMessage{tags={badges=premium/1, emotes=234567:1-4}, badges=[premium/1], "
            + "emotes=[EmoteLocations{startIndex=1, endIndex=4, emoteId='234567'}], permissions=[EVERYONE], "
            + "username='ddellspebot', command='ACTION', payload='payload2', message='message2', "
            + "rawMessage='rawMessage2', actionMessage=true}",
        actualMessage.toString());
  }
}
