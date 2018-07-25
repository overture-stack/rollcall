package bio.overture.rollcall.service;

import bio.overture.rollcall.Index.IndexParser;
import bio.overture.rollcall.Index.ResolvedIndex;
import lombok.SneakyThrows;
import lombok.val;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
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

    return Arrays.stream(indicies).map(IndexParser::parse).collect(toList());
  }

}
