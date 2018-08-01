package bio.overture.rollcall.service;

import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.*;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexServiceTest {

  @ClassRule
  public static GenericContainer esContainer =
    new FixedHostPortGenericContainer("docker.elastic.co/elasticsearch/elasticsearch:6.3.2")
      .withFixedExposedPort(10200, 9200)
      .withFixedExposedPort(10300, 9300)
      .waitingFor(Wait.forHttp("/")) // Wait until elastic start
      .withEnv("discovery.type", "single-node");

  private static String INDEX1 = "file_centric_sd_ygva0e1c_foobar";
  private static String INDEX2 = "file_centric_sd_preasa7s_foobar";
  private static String INDEX3 = "file_centric_sd_46sk55a3_foobar";

  private TransportClient client;
  private IndexService service;

  @Before
  @SneakyThrows
  public void setUp() {
    client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", "docker-cluster").build())
      .addTransportAddress(new TransportAddress(InetAddress.getByName(esContainer.getIpAddress()), 10300));
    service = new IndexService(client);

    client.admin().indices().prepareCreate(INDEX1).get();
    client.admin().indices().prepareCreate(INDEX2).get();
    client.admin().indices().prepareCreate(INDEX3).get();
    client.admin().indices().prepareCreate("badindex").get();

    TimeUnit.SECONDS.sleep(1);
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
  public void getResolvedTest() {
    val resolved = service.getResolved();

    assertThat(resolved).hasSize(3);
    resolved.forEach(i -> assertThat(i.getEntity()).isEqualTo("file"));
    resolved.forEach(i -> assertThat(i.getType()).isEqualTo("centric"));
    resolved.forEach(i -> assertThat(i.getRelease()).isEqualTo("foobar") );
  }

  @Test
  @SneakyThrows
  public void getStateTestNoAlias() {
    service.getState().valuesIt().forEachRemaining(i -> assertThat(i).isEmpty());
  }

  @Test
  @SneakyThrows
  public void releaseAndRemoveTest() {
    val list = Lists.list(INDEX1,INDEX2,INDEX3);

    val added = service.addAlias("file_centric", list);

    assertThat(added).isTrue();

    val state = service.getState();
    list.forEach(index -> {
      val indexState = state.get(index);
      assertThat(indexState).isNotEmpty();
      assertThat(indexState.get(0).alias()).isEqualTo("file_centric");
    });

    val removed = service.removeAlias("file_centric", Lists.list(INDEX1,INDEX2,INDEX3));
    assertThat(removed).isTrue();
    service.getState().valuesIt().forEachRemaining(i -> assertThat(i).isEmpty());
  }

}