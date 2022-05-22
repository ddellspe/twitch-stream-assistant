package net.ddellspe.twitchstreamassistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.ChatBadge;
import com.github.twitch4j.helix.domain.ChatBadgeSet;
import com.github.twitch4j.helix.domain.ChatBadgeSetList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.ddellspe.twitchstreamassistant.config.BotConfiguration;
import net.ddellspe.twitchstreamassistant.plugins.HelpPlugin;
import net.ddellspe.twitchstreamassistant.plugins.WriteChatToChatLogFilePlugin;
import net.ddellspe.twitchstreamassistant.plugins.WriteChatToWebSocketPlugin;
import org.springframework.context.ApplicationContext;

public class Bot {
  private BotConfiguration configuration;
  private final TwitchClient twitchClient;
  private final TwitchClient twitchClientForWebhooks;
  private final ApplicationContext context;
  private final String broadcasterId;

  public Bot(ApplicationContext context) {
    this.context = context;
    loadConfiguration();

    OAuth2Credential credential =
        new OAuth2Credential("twitch", configuration.getCredentials().get("irc"));

    twitchClient =
        TwitchClientBuilder.builder()
            .withClientId(configuration.getApi().get("twitch_client_id"))
            .withClientSecret(configuration.getApi().get("twitch_client_secret"))
            .withEnableHelix(true)
            .withChatAccount(credential)
            .withEnableChat(true)
            .withEnableGraphQL(true)
            .build();

    twitchClientForWebhooks =
        TwitchClientBuilder.builder().withChatAccount(credential).withEnableChat(true).build();

    broadcasterId =
        twitchClient
            .getHelix()
            .getUsers("", null, List.of(configuration.getChannel()))
            .execute()
            .getUsers()
            .get(0)
            .getId();
  }

  public void registerPlugins() {
    SimpleEventHandler inboundMessageEventHandler =
        twitchClientForWebhooks.getEventManager().getEventHandler(SimpleEventHandler.class);
    SimpleEventHandler botResponseEventHandler =
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class);
    WriteChatToWebSocketPlugin chatToWebSocketPlugin =
        context.getBean(WriteChatToWebSocketPlugin.class);
    chatToWebSocketPlugin.setEventHandler(inboundMessageEventHandler);
    WriteChatToChatLogFilePlugin chatToChatLogFilePlugin =
        new WriteChatToChatLogFilePlugin(inboundMessageEventHandler);
    HelpPlugin helpPlugin = new HelpPlugin(botResponseEventHandler);
  }

  public void prepopulateAssets() {
    ChatBadgeSetList globalBadges = twitchClient.getHelix().getGlobalChatBadges(null).execute();
    ChatBadgeSetList channelBadges =
        twitchClient.getHelix().getChannelChatBadges(null, broadcasterId).execute();
    Map<String, Map<String, String>> badges =
        globalBadges.getBadgeSets().stream()
            .collect(
                Collectors.toMap(
                    ChatBadgeSet::getSetId,
                    chatBadgeSet ->
                        chatBadgeSet.getVersions().stream()
                            .collect(
                                Collectors.toMap(ChatBadge::getId, ChatBadge::getMediumImageUrl))));
    badges.putAll(
        channelBadges.getBadgeSets().stream()
            .collect(
                Collectors.toMap(
                    ChatBadgeSet::getSetId,
                    chatBadgeSet ->
                        chatBadgeSet.getVersions().stream()
                            .collect(
                                Collectors.toMap(
                                    ChatBadge::getId, ChatBadge::getMediumImageUrl)))));
    File resourceRoot =
        Paths.get(
                new File(this.getClass().getResource("/").getPath()).getAbsolutePath(),
                "static",
                "img",
                "badges")
            .toFile();
    if (!resourceRoot.exists()) {
      resourceRoot.mkdirs();
    }
    for (Map.Entry<String, Map<String, String>> badgeSet : badges.entrySet()) {
      for (Map.Entry<String, String> badge : badgeSet.getValue().entrySet()) {
        try {
          File file =
              Paths.get(resourceRoot.getAbsolutePath(), badgeSet.getKey() + "_" + badge.getKey())
                  .toFile();
          if (file.createNewFile()) {
            URL url = new URL(badge.getValue());
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
  }

  public void start() {
    twitchClient.getChat().joinChannel(configuration.getChannel());
    twitchClientForWebhooks.getChat().joinChannel(configuration.getChannel());
  }

  private void loadConfiguration() {
    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream("configs/config.yaml");

      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      configuration = mapper.readValue(is, BotConfiguration.class);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("Unable to load BotConfiguration...Exiting");
      System.exit(1);
    }
  }
}
