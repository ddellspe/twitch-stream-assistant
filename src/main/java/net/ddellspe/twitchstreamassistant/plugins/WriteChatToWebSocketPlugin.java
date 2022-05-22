package net.ddellspe.twitchstreamassistant.plugins;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import net.ddellspe.twitchstreamassistant.model.TwitchChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WriteChatToWebSocketPlugin {
  private static final String DOWNLOAD_URL =
      "https://static-cdn.jtvnw.net/emoticons/v2/%s/default/dark/2.0";

  @Autowired private SimpMessagingTemplate messagingTemplate;

  public void setEventHandler(SimpleEventHandler handler) {
    handler.onEvent(ChannelMessageEvent.class, this::onChannelMessage);
    handler.onEvent(ChannelMessageActionEvent.class, this::onChannelMessage);
  }

  public void processAndSendMessageToTopic(TwitchChatMessage message) {
    if (message.getEmotes() != null && message.getEmotes().size() > 0) {
      File resourceRoot =
          Paths.get(
                  new File(this.getClass().getResource("/").getPath()).getAbsolutePath(),
                  "static",
                  "img",
                  "emotes")
              .toFile();
      if (!resourceRoot.exists()) {
        resourceRoot.mkdirs();
      }
      for (TwitchChatMessage.EmoteLocation emote : message.getEmotes()) {
        try {
          File file = Paths.get(resourceRoot.getAbsolutePath(), emote.getEmoteId()).toFile();
          if (file.createNewFile()) {
            URL url = new URL(String.format(DOWNLOAD_URL, emote.getEmoteId()));
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            try (FileOutputStream fos = new FileOutputStream(file)) {
              fos.write(is.readAllBytes());
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    messagingTemplate.convertAndSend("/topic/chat", message);
  }

  public void onChannelMessage(ChannelMessageEvent event) {
    processAndSendMessageToTopic(new TwitchChatMessage(event));
  }

  public void onChannelMessage(ChannelMessageActionEvent event) {
    processAndSendMessageToTopic(new TwitchChatMessage(event));
  }
}
