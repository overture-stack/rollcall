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

package bio.overture.rollcall.repository;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.shrink.ResizeRequest;
import org.elasticsearch.action.admin.indices.shrink.ResizeType;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions.*;

@Repository
public class IndexRepository {

  private final RestHighLevelClient client;

  @Autowired
  public IndexRepository(@NonNull RestHighLevelClient client) {
    this.client = client;
  }

  @SneakyThrows
  public String[] getIndices() {
    return getIndices("*");
  }

  @SneakyThrows
  public String[] getIndices(String... indexName) {
    return client.indices()
      .get(new GetIndexRequest(indexName).indicesOptions(IndicesOptions.lenientExpand()), RequestOptions.DEFAULT)
      .getIndices();
  }

  @SneakyThrows
  public ImmutableOpenMap<String, List<AliasMetaData>> getAliasState() {
    val aliases = client.indices().getAlias(new GetAliasesRequest(), RequestOptions.DEFAULT).getAliases();
    val builder = new ImmutableOpenMap.Builder();

    aliases.forEach((k,v) -> builder.put(k, v.stream().collect(toUnmodifiableList())));

    return builder.build();
  }

  @SneakyThrows
  public boolean removeAlias(@NonNull String alias, @NonNull List<String> indices) {
    return updateIndicesAliases(alias, Collections.emptyList(), indices);
  }

  @SneakyThrows
  public boolean addAlias(@NonNull String alias, @NonNull List<String> indices) {
    return updateIndicesAliases(alias, indices, Collections.emptyList());
  }

  @SneakyThrows
  public boolean updateIndicesAliases(@NonNull String alias, @NonNull List<String> indicesToAddToAlias, @NonNull List<String> indicesToRemoveFromAlias) {
    val req = new IndicesAliasesRequest();

    indicesToRemoveFromAlias.forEach(i -> req.addAliasAction(remove().alias(alias).index(i)));

    indicesToAddToAlias.forEach(i -> req.addAliasAction(add().alias(alias).index(i)));

    if (req.getAliasActions().isEmpty()) {
      return true;
    }

    return client.indices().updateAliases(req, RequestOptions.DEFAULT).isAcknowledged();
  }

  @SneakyThrows
  public boolean makeIndicesReadOnly(@NonNull List<String> indices) {
    final String disableWriteSetting = "{ \"index.blocks.write\": true }";
    val updateSettingsReq = new UpdateSettingsRequest(indices.toArray(String[]::new))
                                    .settings(disableWriteSetting, XContentType.JSON);
    return client.indices().putSettings(updateSettingsReq, RequestOptions.DEFAULT).isAcknowledged();
  }

  @SneakyThrows
  public boolean createIndex(@NonNull String indexName) {
    return createIndex(indexName, "{}");
  }

  @SneakyThrows
  public boolean createIndex(@NonNull String indexName, @NonNull String settings) {
    val req = new CreateIndexRequest(indexName);
    req.settings(settings, XContentType.JSON);
    return client.indices().create(req, RequestOptions.DEFAULT).isAcknowledged();
  }

  @SneakyThrows
  public boolean cloneIndex(@NonNull String indexToClone, @NonNull String newIndexName, @NonNull String settings) {
    val req = new ResizeRequest(newIndexName, indexToClone);
    req.setResizeType(ResizeType.CLONE);
    req.getTargetIndexRequest().settings(settings, XContentType.JSON);
    return client.indices().clone(req, RequestOptions.DEFAULT).isAcknowledged();
  }

  @SneakyThrows
  public boolean deleteIndices(@NonNull String... indices) {
    if (indices.length == 0) {
      return true;
    }
    val request = new DeleteIndexRequest();
    request.indices(indices);
    return client.indices().delete(request, RequestOptions.DEFAULT).isAcknowledged();
  }

  @SneakyThrows
  public Map<String, Date> getIndicesMappedToCreationDate(@NonNull String... indices) {
    if (indices.length == 0) {
      return Map.of();
    }

    val response = client.indices().get(new GetIndexRequest(indices).indicesOptions(IndicesOptions.lenientExpand()), RequestOptions.DEFAULT);

    val indicesSettings = response.getSettings();

    return indicesSettings.entrySet().stream()
            .collect(
                    Collectors.toMap(
                            Map.Entry::getKey, // key is index name
                            e -> new Date(e.getValue().getAsLong("index.creation_date", null))
                    )
            );
  }
}
