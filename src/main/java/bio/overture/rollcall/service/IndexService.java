package bio.overture.rollcall.service;

import bio.overture.rollcall.index.IndexParser;
import bio.overture.rollcall.index.ResolvedIndex;
import lombok.SneakyThrows;
import lombok.val;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class IndexService {

  private final TransportClient client;

  @Autowired
  public IndexService(TransportClient client) {
    this.client = client;
  }

  @SneakyThrows
  public String[] getIndices() {
    return client.admin().indices()
      .getIndex(new GetIndexRequest()).get()
      .getIndices();
  }

  @SneakyThrows
  public List<ResolvedIndex> getResolved() {
    val indicies = client.admin().indices()
      .getIndex(new GetIndexRequest()).get()
      .getIndices();

    return Arrays.stream(indicies).map(IndexParser::parse)
      .filter(ResolvedIndex::isValid)
      .collect(toList());
  }

  @SneakyThrows
  public ImmutableOpenMap<String, List<AliasMetaData>> getState() {
    val aliases =  client.admin().indices()
      .getAliases(new GetAliasesRequest()).get()
      .getAliases();

    return aliases;
  }

  @SneakyThrows
  public boolean removeAlias(String alias, List<String> indices) {
    val req = new IndicesAliasesRequest();
    indices.forEach(i -> {
      req.addAliasAction(IndicesAliasesRequest.AliasActions.remove().alias(alias).index(i));
    });

    if (req.getAliasActions().isEmpty()) {
      return true;
    }
    return client.admin().indices().aliases(req).get().isAcknowledged();
  }

  @SneakyThrows
  public boolean addAlias(String alias, List<String> indices) {
    val req = new IndicesAliasesRequest();
    indices.forEach(i -> {
      req.addAliasAction(IndicesAliasesRequest.AliasActions.add().alias(alias).index(i));
    });
    return client.admin().indices().aliases(req).get().isAcknowledged();
  }

}
