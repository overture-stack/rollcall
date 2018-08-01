package bio.overture.rollcall.service;

import bio.overture.rollcall.config.RollcallConfig;
import bio.overture.rollcall.model.AliasRequest;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.InetAddress;

import static org.junit.Assert.*;

public class AliasServiceTest {

  @ClassRule
  public static GenericContainer esContainer =
    new FixedHostPortGenericContainer("docker.elastic.co/elasticsearch/elasticsearch:6.3.2")
      .withFixedExposedPort(10200, 9200)
      .withFixedExposedPort(10300, 9300)
      .waitingFor(Wait.forHttp("/")) // Wait until elastic start
      .withEnv("discovery.type", "single-node");

  private static String INDEX1 = "file_centric_sd_ygva0e1c_re_foobar1";
  private static String INDEX2 = "file_centric_sd_preasa7s_re_foobar1";
  private static String INDEX3 = "file_centric_sd_preasa7s_re_foobar2";

  private TransportClient client;
  private IndexService indexService;
  private AliasService service;

  @Before
  @SneakyThrows
  public void setUp() {
    client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", "docker-cluster").build())
      .addTransportAddress(new TransportAddress(InetAddress.getByName(esContainer.getIpAddress()), 10300));
    indexService = new IndexService(client);

    val config = new RollcallConfig(Lists.list(new RollcallConfig.ConfiguredAlias("file_centric", "file", "centric")));
    service = new AliasService(config, indexService);

    client.admin().indices().prepareCreate(INDEX1).get();
    client.admin().indices().prepareCreate(INDEX2).get();
    client.admin().indices().prepareCreate(INDEX3).get();
    client.admin().indices().prepareCreate("badindex").get();
  }

  @After
  @SneakyThrows
  public void tearDown() {
    client.admin().indices().delete(new DeleteIndexRequest(INDEX1)).get();
    client.admin().indices().delete(new DeleteIndexRequest(INDEX2)).get();
    client.admin().indices().delete(new DeleteIndexRequest(INDEX3)).get();
    client.admin().indices().delete(new DeleteIndexRequest("badindex")).get();
  }

  @Test
  public void releaseTest() {
    val request1 = new AliasRequest("file_centric", "RE_foobar1", Lists.list("SD_preasa7s", "sd_ygva0e1c"));
    service.release(request1);
    val state1 = indexService.getState();

    val request2 = new AliasRequest("file_centric", "RE_foobar2", Lists.list("SD_preasa7s"));
    service.release(request2);
    val state2 = indexService.getState();

    // TODO: Fix this so it actually asserts
  }

}