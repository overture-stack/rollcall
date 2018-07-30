package bio.overture.rollcall.service;

import bio.overture.rollcall.config.RollcallConfig;
import bio.overture.rollcall.config.RollcallConfig.ConfiguredAlias;
import bio.overture.rollcall.index.ResolvedIndex;
import lombok.Data;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AliasService {

  private final RollcallConfig aliasConfig;
  private final IndexService indexService;

  @Autowired
  public AliasService(RollcallConfig aliasConfig, IndexService indexService) {
    this.aliasConfig = aliasConfig;
    this.indexService = indexService;
  }

  public List<ConfiguredAlias> getConfigured() {
    return aliasConfig.getAliases();
  }

  public List<AliasCandidates> getCandidates() {
    val resolved = indexService.getResolved();
    val configAliases = aliasConfig.getAliases();

    return configAliases.stream()
      .map(alias ->
        new AliasCandidates(alias, resolved.stream()
          .filter(i -> i.getEntity().equals(alias.getEntity()) && i.getType().equals(alias.getType()))
          .collect(toList()))
      )
    .collect(toList());
  }

  @Data
  public class AliasCandidates {
    private final ConfiguredAlias alias;
    private final List<ResolvedIndex> indices;
  }

}
