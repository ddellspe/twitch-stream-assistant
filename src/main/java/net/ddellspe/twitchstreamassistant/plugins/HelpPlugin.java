package net.ddellspe.twitchstreamassistant.plugins;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class HelpPlugin {

  /**
   * Register events of this class with the EventManager/EventHandler
   *
   * @param eventHandler SimpleEventHandler
   */
  public HelpPlugin(SimpleEventHandler eventHandler) {
    eventHandler.onEvent(ChannelMessageEvent.class, this::onChannelMessage);
  }

  /** Subscribe to the ChannelMessage Event and write the output to the console */
  public void onChannelMessage(ChannelMessageEvent event) {
    if (event.getMessage().equals("!ping")) {
      event.getTwitchChat().sendMessage(event.getChannel().getName(), "pong");
    }
  }
}
