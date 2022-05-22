package net.ddellspe.twitchstreamassistant;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TwitchStreamAssistantApplication {

  public static void main(String[] args) {
    ApplicationContext springContext =
        new SpringApplicationBuilder(TwitchStreamAssistantApplication.class).build().run(args);

    Bot bot = new Bot(springContext);
    bot.prepopulateAssets();
    bot.registerPlugins();
    bot.start();
  }
}
