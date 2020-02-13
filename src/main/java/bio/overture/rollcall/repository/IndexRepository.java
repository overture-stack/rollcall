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

import lombok.SneakyThrows;
import lombok.val;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions.*;

@Repository
public class IndexRepository {

  private final RestHighLevelClient client;

  @Autowired
  public IndexRepository(RestHighLevelClient client) {
    this.client = client;
  }

  @SneakyThrows
  public String[] getIndices() {
    return client.indices()
      .get(new GetIndexRequest("*"), RequestOptions.DEFAULT)
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
  public boolean removeAlias(String alias, List<String> indices) {
    val req = new IndicesAliasesRequest();
    indices.forEach(i -> req.addAliasAction(remove().alias(alias).index(i)));

    if (req.getAliasActions().isEmpty()) {
      return true;
    }
    return client.indices().updateAliases(req, RequestOptions.DEFAULT).isAcknowledged();
  }

  @SneakyThrows
  public boolean addAlias(String alias, List<String> indices) {
    val req = new IndicesAliasesRequest();
    indices.forEach(i -> req.addAliasAction(add().alias(alias).index(i)));
    return client.indices().updateAliases(req, RequestOptions.DEFAULT).isAcknowledged();
  }

}
