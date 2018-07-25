package bio.overture.rollcall.Index;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
public class ResolvedIndex {

  public enum Part {
    INDEX, ENTITY, TYPE, SHARD_PREFIX, SHARD, RELEASE
  }

  private final String indexName;

  private final String entity;
  private final String type;
  private final String shardPrefix;
  private final String shard;
  private final String release;

}
