package bio.overture.rollcall.service;

import bio.overture.rollcall.config.RollcallConfig;
import bio.overture.rollcall.config.RollcallConfig.ConfiguredAlias;
import bio.overture.rollcall.index.ResolvedIndex;
import bio.overture.rollcall.model.AliasRequest;
import bio.overture.rollcall.model.Shard;
import lombok.Data;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

  public boolean release(AliasRequest aliasRequest) {
    val alias = aliasRequest.getAlias();

    val removed = remove(aliasRequest);
    if (!removed) {
      throw new IllegalStateException("Failed to remove alias");
    }

    val candidates = this.getCandidates().stream()
      .filter(c -> c.getAlias().getAlias().equals(alias)).findFirst();
    if (!candidates.isPresent()) {
      throw new IllegalStateException("No such alias with index candidates");
    }

    return addAliasToIndices(aliasRequest, candidates.get());
  }

  public boolean remove(AliasRequest aliasRequest) {
    val alias = aliasRequest.getAlias();
    val shards = getShardsFromRequest(aliasRequest);

    val candidates = this.getCandidates().stream()
      .filter(c -> c.getAlias().getAlias().equals(alias))
      .findFirst();
    if (!candidates.isPresent()) {
      throw new IllegalStateException("No such alias with index candidates");
    }

    // TODO: UNIT TEST
    val existing = getIndicesWithAlias(alias);
    val indices = candidates.get().getIndices().stream()
      .filter(i -> shards.stream().
        anyMatch(shard -> shard.matches(i.getShardPrefix(), i.getShard())))
      .filter(i -> existing.contains(i.getIndexName()))
      .map(ResolvedIndex::getIndexName)
      .collect(toList());

    return indexService.removeAlias(alias, indices);
  }

  private boolean removeAliasFromAllIndices(String alias) {
    val state = indexService.getState();
    List<String> existing = getIndicesWithAlias(alias);
    return indexService.removeAlias(alias, existing);
  }

  private boolean addAliasToIndices(AliasRequest aliasRequest, AliasCandidates candidates) {
    val release = aliasRequest.getRelease().toLowerCase().split("_");
    val shards = getShardsFromRequest(aliasRequest);

    val indices = candidates.getIndices().stream()
      .filter(i -> shards.stream().
        anyMatch(shard -> shard.matches(i.getShardPrefix(), i.getShard()))) // Only update the shards we are interested in
      .filter(i -> i.getReleasePrefix().equals(release[0]) && i.getRelease().equals(release[1]))
      .map(ResolvedIndex::getIndexName)
      .collect(toList());

    return indexService.addAlias(aliasRequest.getAlias(), indices);
  }

  private static List<Shard> getShardsFromRequest(AliasRequest aliasRequest) {
    return aliasRequest.getShards().stream()
      .map(String::toLowerCase)
      .map(s -> s.split("_"))
      .map(s -> new Shard(s[0], s[1]))
      .collect(toList());
  }

  private List<String> getIndicesWithAlias(String alias) {
    val state = indexService.getState();

    List<String> existing = new ArrayList<>();
    state.forEach( entry -> {
      val indexName = entry.key;
      val aliases = entry.value;

      val foundOpt = aliases.stream()
        .filter(a -> a.alias().equals(alias))
        .findFirst();

      if (foundOpt.isPresent()) {
        existing.add(indexName);
      }
    });
    return existing;
  }

  @Data
  public class AliasCandidates {
    private final ConfiguredAlias alias;
    private final List<ResolvedIndex> indices;
  }

}
