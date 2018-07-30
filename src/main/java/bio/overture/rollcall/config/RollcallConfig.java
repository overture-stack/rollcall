package bio.overture.rollcall.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("rollcall")
public class RollcallConfig {

  private List<ConfiguredAlias> aliases;

  public RollcallConfig() {}

  public RollcallConfig(List<ConfiguredAlias> aliases) {
    this.aliases = aliases;
  }

  public List<ConfiguredAlias> getAliases() {
    return aliases;
  }

  public void setAliases( List<ConfiguredAlias> aliases) {
    this.aliases = aliases;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ConfiguredAlias {
    private String alias;
    private String entity;
    private String type;
  }

}
