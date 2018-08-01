package bio.overture.rollcall.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class Shard {

  private final String shardPrefix;
  private final String shardId;

  public boolean matches(@NonNull String prefix, @NonNull String id) {
    return shardPrefix.equals(prefix) && shardId.equals(id);
  }

}
