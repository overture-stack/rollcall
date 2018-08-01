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

package bio.overture.rollcall.index;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResolvedIndex {

  public enum Part {
    INDEX, ENTITY, TYPE, SHARD_PREFIX, SHARD, RELEASE_PREFIX, RELEASE
  }

  private final String indexName;

  private final String entity;
  private final String type;
  private final String shardPrefix;
  private final String shard;
  private final String releasePrefix;
  private final String release;

  public boolean isValid() {
    return !(entity == null
      || type == null
      || shardPrefix == null
      || shard == null
      || releasePrefix == null
      || release == null);
  }

}
