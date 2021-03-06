/*
 * Copyright (c) 2020. The Ontario Institute for Cancer Research. All rights reserved.
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
import bio.overture.rollcall.index.IndexParser;
import bio.overture.rollcall.index.ResolvedIndex;
import bio.overture.rollcall.model.CreateResolvableIndexRequest;
import bio.overture.rollcall.repository.IndexRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@Slf4j
public class IndexService {

    private final IndexRepository repository;
    private final RollcallConfig rollcallConfig;

    @Autowired
    public IndexService(RollcallConfig rollcallConfig, @NonNull IndexRepository repository) {
        this.repository = repository;
        this.rollcallConfig = rollcallConfig;
    }

    public List<ResolvedIndex> getResolved() {
        return Arrays.stream(repository.getIndices()).map(IndexParser::parse)
                       .filter(ResolvedIndex::isValid)
                       .collect(toUnmodifiableList());
    }

    public ResolvedIndex createResolvableIndex(@NonNull CreateResolvableIndexRequest createResolvableIndexRequest) {
        val cloneFromReleasedIndex = createResolvableIndexRequest.getCloneFromReleasedIndex();
        val newIndexSettings = createResolvableIndexRequest.getIndexSetting();

        // parse request into a dummyResolvedIndex to make sure the parameters are valid, release version is irrelevant
        val dummyResolvedIndex = generateResolvedIndex(createResolvableIndexRequest, 1);

        // get existing resolved indices with the same alias and shard values
        val existingResolvedIndices = getRelevantResolvedIndices(dummyResolvedIndex);

        // find resolved index with latest release value in existing resolved indices
        val latestResolvedIndex = existingResolvedIndices.stream().max(ResolvedIndexByReleaseComparator);

        // create new resolved index, with new release value
        val newReleaseValue = latestResolvedIndex.isEmpty() ? 1 : calculateNewReleaseValue(latestResolvedIndex.get());
        val newResolvedIndex = generateResolvedIndex(createResolvableIndexRequest, newReleaseValue);
        val newIndexName = newResolvedIndex.getIndexName();

        // indexToClone is the published/released index, not the latestResolvedIndex
        val releasedIndexToClone = findReleasedIndexLikeResolvedIndex(newResolvedIndex);

        if (releasedIndexToClone.isPresent() && cloneFromReleasedIndex) {
            val indexNameToClone = releasedIndexToClone.get().getIndexName();
            log.info("Index to clone: " + indexNameToClone);
            repository.cloneIndex(indexNameToClone, newIndexName, newIndexSettings);
        } else {
            repository.createIndex(newIndexName, newIndexSettings);
        }

        log.info("New cloned/created index name: " + newIndexName);
        return newResolvedIndex;
    }

    // This function is used to find a released index which has the same enity & type (which is configured with an alias)
    // as well as the same shardName, shardPrefix and releasePrefix as the resolvedIndex to be created
    private Optional<ResolvedIndex> findReleasedIndexLikeResolvedIndex(ResolvedIndex resolvedIndexToCompareWith) {
        val alias = getRelevantAlias(resolvedIndexToCompareWith.getEntity(), resolvedIndexToCompareWith.getType());
        if (alias.isEmpty()) {
            return Optional.empty();
        }
        val shard = resolvedIndexToCompareWith.getShard();
        val shardPrefix = resolvedIndexToCompareWith.getShardPrefix();
        val releasePrefix = resolvedIndexToCompareWith.getReleasePrefix();

        val relevantIndecies = repository.getIndices(alias.get());

        return Arrays.stream(relevantIndecies)
                .map(IndexParser::parse)
                .filter(i -> i.getShard().equals(shard) && i.getShardPrefix().equals(shardPrefix) && i.getReleasePrefix().equals(releasePrefix))
                .findFirst();
    }

    private List<ResolvedIndex> getRelevantResolvedIndices(ResolvedIndex testResolvableIndex) {
        val type = testResolvableIndex.getType();
        val entity = testResolvableIndex.getEntity();
        val shard = testResolvableIndex.getShard();
        val shardPrefix = testResolvableIndex.getShardPrefix();

        return getResolved().stream()
                .filter(i -> i.getShard().equals(shard)
                         && i.getShardPrefix().equals(shardPrefix)
                         && i.getEntity().equals(entity)
                         && i.getType().equals(type))
                .collect(toUnmodifiableList());
    }

    private Optional<String> getRelevantAlias(String entity, String type) {
        return rollcallConfig.getAliases()
               .stream()
               .filter(ca ->  ca.getEntity().equals(entity) && ca.getType().equals(type))
               .map(RollcallConfig.ConfiguredAlias::getAlias)
               .findFirst();
    }

    private int calculateNewReleaseValue(ResolvedIndex indexToRef) {
        int[] currReleaseIntParts = getReleaseIntegerParts(indexToRef.getRelease());
        int newRelease;
        try {
            newRelease = currReleaseIntParts[0];
            newRelease +=  currReleaseIntParts.length == 1 ? 1 : 0; // increment if previous release was major with no extensions (e.g. beta)
        } catch (NumberFormatException e) {
            newRelease = 1;
        }
        return newRelease;
    }

    private ResolvedIndex generateResolvedIndex(CreateResolvableIndexRequest createResolvableIndexRequest, int release) {
        val indexName = createResolvableIndexRequest.getEntity() + '_'
                + createResolvableIndexRequest.getType() + '_'
                + createResolvableIndexRequest.getShardPrefix() + '_'
                + createResolvableIndexRequest.getShard() + '_'
                + createResolvableIndexRequest.getReleasePrefix() + '_' + release;
        return IndexParser.parse(indexName);
    }

    private static int[] getReleaseIntegerParts(String releaseStr) {
        return Arrays.stream(releaseStr.split("[^\\d]+")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
    }

    public static final Comparator<ResolvedIndex> ResolvedIndexByReleaseComparator = (firstResolvedIndex, secondResolvedIndex) -> {
        if (firstResolvedIndex.getRelease().equals((secondResolvedIndex.getRelease()))) {
            return 0;
        }

        val firstIndexReleaseIntParts = getReleaseIntegerParts(firstResolvedIndex.getRelease());
        val secondIndexReleaseIntParts = getReleaseIntegerParts(secondResolvedIndex.getRelease());

        // first consider major int part only
        if (firstIndexReleaseIntParts.length == 1 && firstIndexReleaseIntParts[0] >= secondIndexReleaseIntParts[0]) {
            return 1;
        } else if (secondIndexReleaseIntParts.length == 1  && firstIndexReleaseIntParts[0] <= secondIndexReleaseIntParts[0]) {
            return -1;
        }

        // next compare the integer values of both indices, going through each pair until finding a pair that are not equal
        int i = 1;
        while (i < firstIndexReleaseIntParts.length && i < secondIndexReleaseIntParts.length) {
            if (firstIndexReleaseIntParts[i] > secondIndexReleaseIntParts[i])
                return 1;
            else if (firstIndexReleaseIntParts[i] < secondIndexReleaseIntParts[i])
                return -1;
            i++; // both are equal, so keep looking
        }

        return 0; // found no difference, both have same release integer pairs
    };
}
