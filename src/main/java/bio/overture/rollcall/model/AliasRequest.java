package bio.overture.rollcall.model;

import lombok.Data;

import java.util.List;

@Data
public class AliasRequest {

  private final String alias;
  private final String release;
  private final List<String> shards;

}