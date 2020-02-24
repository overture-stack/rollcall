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

import bio.overture.rollcall.index.IndexParser;
import bio.overture.rollcall.index.ResolvedIndex;
import bio.overture.rollcall.model.CreateResolvableIndexRequest;
import bio.overture.rollcall.repository.IndexRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IndexService {

    private final IndexRepository repository;
    private final AliasService aliasService;

    @Autowired
    public IndexService(@NonNull IndexRepository repository, @NonNull AliasService aliasService) {
        this.repository = repository;
        this.aliasService = aliasService;
    }

    public List<ResolvedIndex> getResolved() {  return aliasService.getResolved();  }

    public ResolvedIndex createResolvableIndex(@NonNull CreateResolvableIndexRequest createResolvableIndexRequest) {
        val clone = createResolvableIndexRequest.getClone();
        val newIndexSettings = createResolvableIndexRequest.getIndexSetting();

        // parse request into a dummyResolvedIndex to make sure the parameters are valid, release version is irrelevant here
        val dummyResolvedIndex = generateResolvedIndex(createResolvableIndexRequest, 1);

        // get existing resolved indices with the same alias and shard values
        val existingResolvedIndices = getRelevantResolvedIndices(dummyResolvedIndex);

        // find resolved index with latest release value in existing resolved indices
        val latestResolvedIndex = existingResolvedIndices.stream().max(ResolvedIndexByReleaseComparator);

        // get release value for new index to be created
        val newReleaseValue = calculateNewReleaseValue(latestResolvedIndex);

        // generate resolved index object, with new release value to make sure it is still valid
        val newResolvedIndex = generateResolvedIndex(createResolvableIndexRequest, newReleaseValue);

        val newIndexName = newResolvedIndex.getIndexName();

        // clone if requested and possible to do so, otherwise create
        if (latestResolvedIndex.isPresent() && clone) {
            val latestResolvedIndexName = latestResolvedIndex.get().getIndexName();
            log.info("Index to clone: " + latestResolvedIndexName);
            repository.cloneIndex(latestResolvedIndexName, newIndexName, newIndexSettings);
        } else {
            repository.createIndex(newIndexName, newIndexSettings);
        }

        log.info("New cloned/created index name: " + newIndexName);
        return newResolvedIndex;
    }

    private List<ResolvedIndex> getRelevantResolvedIndices(ResolvedIndex testResolvableIndex) {
        val alias = testResolvableIndex.getEntity() + '_' + testResolvableIndex.getType();
        val shard = testResolvableIndex.getShard();
        val shardPrefix = testResolvableIndex.getShardPrefix();
        return aliasService
                .getRelevantCandidates(alias)
                .getIndices().stream()
                .filter(i -> i.getShard().equals(shard) && i.getShardPrefix().equals(shardPrefix))
                .collect(Collectors.toList());
    }

    private int calculateNewReleaseValue(Optional<ResolvedIndex> indexToRef) {
        if (indexToRef.isEmpty()) {
            return 1;
        }

        int[] currReleaseIntParts = getReleaseIntegerParts(indexToRef.get().getRelease());
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

        // first consider major releases with no additional numbers
        if (firstIndexReleaseIntParts.length == 1 && firstIndexReleaseIntParts[0] >= secondIndexReleaseIntParts[0]) {
            return 1;
        } else if (secondIndexReleaseIntParts.length == 1  && firstIndexReleaseIntParts[0] <= secondIndexReleaseIntParts[0]) {
            return -1;
        }

        // next compare the integer values of both indices, going through each pair until finding a pair that are not equal
        int i = 1;
        while (i < firstIndexReleaseIntParts.length && i < secondIndexReleaseIntParts.length) {
            int firstIndexReleaseIntPart = firstIndexReleaseIntParts[i];
            int secondIndexReleaseIntPart = secondIndexReleaseIntParts[i];

            if (firstIndexReleaseIntPart > secondIndexReleaseIntPart)
                return 1;
            else if (firstIndexReleaseIntPart < secondIndexReleaseIntPart)
                return -1;

            // both are equal, so keep looking
            i++;
        }

        // found no difference, both have same release integer pairs
        return 0;
    };
}
