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

    public List<ResolvedIndex> getResolved() {
        return aliasService.getResolved();
    }

    public ResolvedIndex createResolvableIndex(@NonNull CreateResolvableIndexRequest createResolvableIndexRequest) {
        val alias = createResolvableIndexRequest.getEntity() + '_' + createResolvableIndexRequest.getType();
        val shard = createResolvableIndexRequest.getShard();
        val shardPrefix = createResolvableIndexRequest.getShardPrefix();

        // get relevant candidates for alias
        val aliasCandidates = aliasService.getRelevantCandidates(alias);
        val existingIndices = aliasCandidates.getIndices().stream()
                .filter(i -> i.getShard().equals(shard) && i.getShardPrefix().equals(shardPrefix))
                .collect(Collectors.toList());

        // no existing index with given shard prefix + shard name, so make new one
        if (existingIndices.size() == 0) {
            val newIndexName = generateNewIndexName(createResolvableIndexRequest, 1);
            repository.createIndex(newIndexName);
            return IndexParser.parse(newIndexName);
        }

        // find resolved index with latest release
        val indexOfInterest = existingIndices.stream().max(releaseComparator).get();

        // make new index name - with release version incremented
        val newIndexName = generateNewIndexName(createResolvableIndexRequest, indexOfInterest);

        // create or clone
        log.info("Index to clone: " + indexOfInterest.getIndexName());
        log.info("New cloned/created index name: " + newIndexName);
        if (createResolvableIndexRequest.getClone()) {
            repository.cloneIndex(indexOfInterest.getIndexName(), newIndexName);
        } else  {
            repository.createIndex(newIndexName);
        }

        // return new index details
        return IndexParser.parse(newIndexName);
    }

    private String generateNewIndexName(CreateResolvableIndexRequest createResolvableIndexRequest, ResolvedIndex indexToRef) {
        int[] currReleaseInts = getReleaseIntegerParts(indexToRef.getRelease()); //take the release ints
        int newRelease;
        try {
            newRelease = currReleaseInts[0];
            newRelease +=  currReleaseInts.length == 1 ? 1 : 0; // increment if previous release was major only
        } catch (NumberFormatException e) {
            newRelease = 1;
        }

        return  generateNewIndexName(createResolvableIndexRequest, newRelease);
    }

    private String generateNewIndexName(CreateResolvableIndexRequest createResolvableIndexRequest, int release) {
        return  createResolvableIndexRequest.getEntity() + '_'
                + createResolvableIndexRequest.getType() + '_'
                + createResolvableIndexRequest.getShardPrefix() + '_'
                + createResolvableIndexRequest.getShard() + '_'
                + createResolvableIndexRequest.getReleasePrefix() + '_' + release;
    }

    private Comparator<ResolvedIndex> releaseComparator = (firstIndex, secondIndex) -> {
        if (firstIndex.getRelease().equals((secondIndex.getRelease()))) {
            return 0;
        }

        val firstIndexReleaseIntParts = getReleaseIntegerParts(firstIndex.getRelease());
        val secondIndexReleaseIntParts = getReleaseIntegerParts(secondIndex.getRelease());

        // consider major releases with no additional numerics
        if (firstIndexReleaseIntParts.length == 1 && firstIndexReleaseIntParts[0] >= secondIndexReleaseIntParts[0]) {
            return 1;
        } else if (secondIndexReleaseIntParts.length == 1  && firstIndexReleaseIntParts[0] <= secondIndexReleaseIntParts[0]) {
            return -1;
        }

        // compare the integer values of both indices, going through each pair until finding a pair that are not equal
        int i = 0;
        while (i < firstIndexReleaseIntParts.length && i < secondIndexReleaseIntParts.length) {
            int firstIndexRelease = firstIndexReleaseIntParts[i];
            int secondIndexRelease = secondIndexReleaseIntParts[i];

            if (firstIndexRelease > secondIndexRelease)
                return 1;
            else if (firstIndexRelease < secondIndexRelease)
                return -1;

            // both are equal, so keep looking
            i++;
        }

        // found no difference, both have same major release ints
        return 0;
    };

    private int[] getReleaseIntegerParts(String releaseStr) {
        return Arrays.stream(releaseStr.split("[^\\d]+")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
    }
}
