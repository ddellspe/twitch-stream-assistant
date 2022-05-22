package net.ddellspe.twitchstreamassistant.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotConfiguration {
  private Boolean debug;

  private Map<String, String> api;

  private Map<String, String> credentials;

  private String channel;

  public Boolean getDebug() {
    return debug;
  }

  public void setDebug(Boolean debug) {
    this.debug = debug;
  }

  public Map<String, String> getApi() {
    return api;
  }

  public void setApi(Map<String, String> api) {
    this.api = api;
  }

  public Map<String, String> getCredentials() {
    return credentials;
  }

  public void setCredentials(Map<String, String> credentials) {
    this.credentials = credentials;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  @Override
  public String toString() {
    return "BotConfiguration{"
        + "debug="
        + debug
        + ", api="
        + api
        + ", credentials="
        + credentials
        + ", channel='"
        + channel
        + '\''
        + '}';
  }
}
