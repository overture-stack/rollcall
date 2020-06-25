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

package bio.overture.rollcall.service;

import bio.overture.rollcall.config.RollcallConfig;
import bio.overture.rollcall.config.RollcallConfig.ConfiguredAlias;
import bio.overture.rollcall.exception.NoSuchAliasWithCandidatesException;
import bio.overture.rollcall.exception.ReleaseIntegrityException;
import bio.overture.rollcall.index.IndexParser;
import bio.overture.rollcall.index.ResolvedIndex;
import bio.overture.rollcall.model.AliasRequest;
import bio.overture.rollcall.model.Shard;
import bio.overture.rollcall.repository.IndexRepository;
import lombok.Data;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
public class AliasService {

  private final RollcallConfig aliasConfig;
  private final IndexRepository repository;

  @Autowired
  public AliasService(@NonNull RollcallConfig aliasConfig, @NonNull IndexRepository repository) {
    this.aliasConfig = aliasConfig;
    this.repository = repository;
  }

  public List<ConfiguredAlias> getConfigured() {
    return aliasConfig.getAliases();
  }

  public List<ResolvedIndex> getResolved() {
    return Arrays.stream(repository.getIndices()).map(IndexParser::parse)
      .filter(ResolvedIndex::isValid)
      .collect(toList());
  }

  public List<AliasCandidates> getCandidates() {
    val resolved = this.getResolved();
    val configAliases = aliasConfig.getAliases();

    return configAliases.stream()
      .map(alias ->
        new AliasCandidates(alias, resolved.stream()
          .filter(i -> i.getEntity().equals(alias.getEntity()) && i.getType().equals(alias.getType()))
          .collect(toList()))
      )
    .collect(toList());
  }

  public AliasCandidates getRelevantCandidates(String alias) {
    val candidatesOpt = this.getCandidates()
            .stream()
            .filter(c -> c.getAlias().getAlias().equals(alias))
            .findFirst();
    if (candidatesOpt.isEmpty()) {
      throw new NoSuchAliasWithCandidatesException("No such alias with index candidates");
    }
    return candidatesOpt.get();
  }

  public boolean release(@NonNull AliasRequest aliasRequest) {
    val alias = aliasRequest.getAlias();
    val releases = getReleasesFromRequest(aliasRequest);
    val shards = getShardsFromRequest(aliasRequest);

    // First identify candidates and check existence of at least one.
    val candidates = getRelevantCandidates(alias);

    // Now do a pre-flight check to see if target indices exist and resolve correctly before continuing.
    val indicesToRelease = getIndicesForRelease(candidates, releases, shards);
    if (indicesToRelease.size() != aliasRequest.getShards().size()) {
      throw new ReleaseIntegrityException(aliasRequest.getRelease(), aliasRequest.getShards(), indicesToRelease);
    }

    val indicesToRemoveFromAlias = getIndicesToRemoveFromAlias(aliasRequest);
    val indicesToDelete = getIndicesToDelete(candidates, shards, indicesToRelease);

    val successfullyMadeReadOnly = repository.makeIndicesReadOnly(indicesToRelease);
    val successfullyUpdatedAliases = repository.updateIndicesAliases(alias, indicesToRelease, indicesToRemoveFromAlias);
    val successfullyDeletedOldIndices = repository.deleteIndices(indicesToDelete.toArray(String[]::new));

    return successfullyMadeReadOnly && successfullyUpdatedAliases && successfullyDeletedOldIndices;
  }

  public List<String> getIndicesToRemoveFromAlias(@NonNull AliasRequest aliasRequest) {
    val alias = aliasRequest.getAlias();
    val shards = getShardsFromRequest(aliasRequest);

    val candidates = getRelevantCandidates(alias);

    // TODO: UNIT TEST
    val existing = getIndicesWithAlias(alias);
    return candidates.getIndices().stream()
      .filter(i -> shards.stream().
        anyMatch(shard -> shard.matches(i.getShardPrefix(), i.getShard())))
      .filter(i -> existing.contains(i.getIndexName()))
      .map(ResolvedIndex::getIndexName)
      .collect(toList());
  }

  public boolean remove(@NonNull AliasRequest aliasRequest) {
    val alias = aliasRequest.getAlias();
    val indices = getIndicesToRemoveFromAlias(aliasRequest);
    return repository.removeAlias(alias, indices);
  }

  private static List<Shard> getShardsFromRequest(AliasRequest aliasRequest) {
    return aliasRequest.getShards().stream()
      .map(String::toLowerCase)
      .map(s -> s.split("_"))
      .map(s -> new Shard(s[0], s[1]))
      .collect(toList());
  }

  private static String[] getReleasesFromRequest(AliasRequest aliasRequest) {
    return aliasRequest.getRelease().toLowerCase().split("_");
  }

  private List<String> getIndicesWithAlias(String alias) {
    val state = repository.getAliasState();

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

  private List<String> getIndicesForRelease(AliasCandidates candidates, String[] release, List<Shard> shards) {
    return candidates.getIndices().stream()
      .filter(i -> shards.stream().
        anyMatch(shard -> shard.matches(i.getShardPrefix(), i.getShard()))) // Only update the shards we are interested in
      .filter(i -> i.getReleasePrefix().equals(release[0]) && i.getRelease().equals(release[1]))
      .map(ResolvedIndex::getIndexName)
      .collect(toList());
  }

  private List<String> getIndicesToDelete(AliasCandidates candidates, List<Shard> shards, List<String> releaseIndicesToIgnore) {
    val numOfRecentIndicesToKeep = candidates.getAlias().getReleaseRotation();
    if ( numOfRecentIndicesToKeep < 0) {
      return Collections.emptyList();
    }

    val relevantIndices = candidates.getIndices().stream()
                          .filter(i -> shards.stream().anyMatch(shard -> shard.matches(i.getShardPrefix(), i.getShard())))
                          .map(ResolvedIndex::getIndexName)
                          .filter(i -> !releaseIndicesToIgnore.contains(i))
                          .collect(toList());

    val sortedByDate = getIndicesSortedByCreationDate(relevantIndices);
    val deleteUpToIndex = Math.max(sortedByDate.size() - numOfRecentIndicesToKeep, 0);

    return sortedByDate.subList(0, deleteUpToIndex);
  }

  private List<String> getIndicesSortedByCreationDate(List<String> indexNames) {
    val indexMappedToCreationDate = repository.getIndicesMappedToCreationDate(indexNames.toArray(String[]::new));
    return indexMappedToCreationDate.entrySet().stream()
                   .sorted(Map.Entry.comparingByValue()) // sort by map value, which is date
                   .map(Map.Entry::getKey)
                   .collect(toList());
  }

  @Data
  public class AliasCandidates {
    private final ConfiguredAlias alias;
    private final List<ResolvedIndex> indices;
  }

}
