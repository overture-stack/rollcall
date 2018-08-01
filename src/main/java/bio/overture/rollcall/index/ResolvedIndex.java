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
