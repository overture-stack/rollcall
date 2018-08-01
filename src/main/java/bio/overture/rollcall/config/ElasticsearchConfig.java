package bio.overture.rollcall.config;

import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Configuration
public class ElasticsearchConfig {

  private static final String CLUSTER_NAME = "cluster.name";

  @Value("${elasticsearch.host}")
  private String host;

  @Value("${elasticsearch.port}")
  private int port;

  @Value("${elasticsearch.cluster-name}")
  private String clusterName;

  @Bean
  @SneakyThrows
  public RestHighLevelClient restClient() {
    return new RestHighLevelClient(
      RestClient.builder(
        new HttpHost(InetAddress.getByName(host), port)
      )
    );
  }

  @Bean
  @SneakyThrows
  public TransportClient transportClient() {
    return new PreBuiltTransportClient(Settings.builder()
        .put(CLUSTER_NAME, clusterName)
        .build())
      .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
  }

}
