package net.ddellspe.twitchstreamassistant.plugins;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class WriteChatToChatLogFilePlugin {

  /**
   * Register events of this class with the EventManager/EventHandler
   *
   * @param eventHandler SimpleEventHandler
   */
  public WriteChatToChatLogFilePlugin(SimpleEventHandler eventHandler) {
    eventHandler.onEvent(ChannelMessageEvent.class, this::onChannelMessage);
    eventHandler.onEvent(ChannelMessageActionEvent.class, this::onChannelMessage);
  }

  /** Subscribe to the ChannelMessage Event and write the output to the console */
  public void onChannelMessage(ChannelMessageEvent event) {
    System.out.printf(
        "Channel [%s] - User[%s] - Message [%s]%n",
        event.getChannel().getName(), event.getUser().getName(), event.getMessage());
    System.out.println(event.getMessageEvent().getRawMessage());
  }

  /** Subscribe to the ChannelMessage Event and write the output to the console */
  public void onChannelMessage(ChannelMessageActionEvent event) {
    System.out.printf(
        "Channel [%s] - User[%s] - Message * [%s] *%n",
        event.getChannel().getName(), event.getUser().getName(), event.getMessage());
    System.out.println(event.getMessageEvent().getRawMessage());
  }
}
