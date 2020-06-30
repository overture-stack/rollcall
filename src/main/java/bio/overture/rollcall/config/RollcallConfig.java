/*
 * Copyright (c) 2018. The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package bio.overture.rollcall.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("rollcall")
public class RollcallConfig {

  private List<ConfiguredAlias> aliases;

  public RollcallConfig() {}

  public RollcallConfig(@NonNull List<ConfiguredAlias> aliases) {
    this.aliases = aliases;
  }

  public List<ConfiguredAlias> getAliases() {
    return aliases;
  }

  public void setAliases(@NonNull List<ConfiguredAlias> aliases) {
    this.aliases = aliases;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @RequiredArgsConstructor
  public static class ConfiguredAlias {
    @NonNull private String alias;
    @NonNull private String entity;
    @NonNull private String type;
    private int releaseRotation = -1;
  }

}
