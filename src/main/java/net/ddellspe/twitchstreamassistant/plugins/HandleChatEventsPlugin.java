package net.ddellspe.twitchstreamassistant.plugins;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.AbstractChannelEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import net.ddellspe.twitchstreamassistant.config.BotConfiguration;
import net.ddellspe.twitchstreamassistant.model.TwitchChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class HandleChatEventsPlugin {
  private static final String DOWNLOAD_URL =
      "https://static-cdn.jtvnw.net/emoticons/v2/%s/default/dark/2.0";
  private static final Logger LOGGER = LoggerFactory.getLogger(HandleChatEventsPlugin.class);
  private static final File LOG_ROOT =
      Paths.get(
              new File(HandleChatEventsPlugin.class.getResource("/").getPath()).getAbsolutePath(),
              "logs")
          .toFile();

  private boolean logMessages;
  private boolean logRawMessages;

  @Autowired private SimpMessagingTemplate messagingTemplate;

  public void setEventHandler(SimpleEventHandler handler, BotConfiguration config) {
    logMessages = config.getFeatures().get("log").equals("true");
    logRawMessages = config.getFeatures().get("log_raw").equals("true");
    handler.onEvent(ChannelMessageEvent.class, event -> onChannelMessage(event));
    handler.onEvent(ChannelMessageActionEvent.class, this::onChannelMessage);
    if (!LOG_ROOT.exists()) {
      LOG_ROOT.mkdirs();
    }
  }

  public void onChannelMessage(ChannelMessageEvent event) {
    if (logMessages || logRawMessages) {
      logChannelMessage(event);
    }
    processAndSendMessageToTopic(new TwitchChatMessage(event));
  }

  public void onChannelMessage(ChannelMessageActionEvent event) {
    if (logMessages || logRawMessages) {
      logChannelMessage(event);
    }
    processAndSendMessageToTopic(new TwitchChatMessage(event));
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

  private void logChannelMessage(AbstractChannelEvent event) {
    if (!logMessages && !logRawMessages) {
      return;
    }
    final String username;
    final String message;
    final String rawMessage;
    final Instant messageDate = event.getFiredAtInstant();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    final String fileDate = df.format(Date.from(messageDate));
    if (event instanceof ChannelMessageEvent) {
      ChannelMessageEvent channelMessageEvent = (ChannelMessageEvent) event;
      username = channelMessageEvent.getUser().getName();
      message = channelMessageEvent.getMessage();
      rawMessage = channelMessageEvent.getMessageEvent().getRawMessage();
    } else if (event instanceof ChannelMessageActionEvent) {
      ChannelMessageActionEvent channelMessageActionEvent = (ChannelMessageActionEvent) event;
      username = channelMessageActionEvent.getUser().getName();
      message = channelMessageActionEvent.getMessage();
      rawMessage = channelMessageActionEvent.getMessageEvent().getRawMessage();
    } else {
      return;
    }
    File chatFile = Paths.get(LOG_ROOT.getAbsolutePath(), fileDate + "_chat.log").toFile();
    File rawFile = Paths.get(LOG_ROOT.getAbsolutePath(), fileDate + "_raw.log").toFile();
    try {
      if (logMessages && chatFile.createNewFile()) {
        LOGGER.debug("Created new chat file {}", chatFile.getAbsolutePath());
      }
      if (logRawMessages && rawFile.createNewFile()) {
        LOGGER.debug("Created new raw message file {}", rawFile.getAbsolutePath());
      }
    } catch (IOException e) {
      LOGGER.error("Failed to create a log file", e);
      throw new RuntimeException(e);
    }
    try (PrintWriter chatWriter = new PrintWriter(new FileWriter(chatFile, true));
        PrintWriter rawWriter = new PrintWriter(new FileWriter(rawFile, true))) {
      SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
      if (logMessages) {
        String logMessage =
            String.format(
                "[%s] %s: %s", timeFormat.format(Date.from(messageDate)), username, message);
        chatWriter.println(logMessage);
        LOGGER.info(logMessage);
      }
      if (logRawMessages) {
        rawWriter.println(rawMessage);
        LOGGER.info(rawMessage);
      }
    } catch (IOException e) {
      LOGGER.error("Error Writing messages to log file", e);
      throw new RuntimeException(e);
    }
  }
}
