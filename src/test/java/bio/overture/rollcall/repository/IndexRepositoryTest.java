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
import org.apache.http.HttpHost;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.junit.*;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexRepositoryTest {

  @ClassRule
  public static GenericContainer esContainer =
    new FixedHostPortGenericContainer("docker.elastic.co/elasticsearch/elasticsearch:7.5.2")
      .withFixedExposedPort(10200, 9200)
      .withFixedExposedPort(10300, 9300)
      .waitingFor(Wait.forHttp("/")) // Wait until elastic start
      .withEnv("discovery.type", "single-node");

  private static String INDEX1 = "file_centric_sd_ygva0e1c_re_foobar";
  private static String INDEX2 = "file_centric_sd_preasa7s_re_foobar";
  private static String INDEX3 = "file_centric_sd_46sk55a3_re_foobar";

  private RestHighLevelClient client;
  private IndexRepository repository;

  @Before
  @SneakyThrows
  public void setUp() {
    client = new RestHighLevelClient( RestClient.builder(new HttpHost(InetAddress.getByName(esContainer.getIpAddress()), 10200)));
    repository = new IndexRepository(client);

    client.indices().create(new CreateIndexRequest(INDEX1), RequestOptions.DEFAULT);
    client.indices().create(new CreateIndexRequest(INDEX2), RequestOptions.DEFAULT);
    client.indices().create(new CreateIndexRequest(INDEX3), RequestOptions.DEFAULT);
    client.indices().create(new CreateIndexRequest("badindex"), RequestOptions.DEFAULT);

    TimeUnit.SECONDS.sleep(1);
  }

  @After
  @SneakyThrows
  public void tearDown() {
    client.indices().delete(new DeleteIndexRequest(INDEX1), RequestOptions.DEFAULT);
    client.indices().delete(new DeleteIndexRequest(INDEX2), RequestOptions.DEFAULT);
    client.indices().delete(new DeleteIndexRequest(INDEX3), RequestOptions.DEFAULT);
    client.indices().delete(new DeleteIndexRequest("badindex"), RequestOptions.DEFAULT);
  }

  @Test
  @SneakyThrows
  public void getStateTestNoAlias() {
    repository.getAliasState().valuesIt().forEachRemaining(i -> assertThat(i).isEmpty());
  }

  @Test
  @SneakyThrows
  public void releaseAndRemoveTest() {
    val list = Lists.list(INDEX1,INDEX2,INDEX3);

    val added = repository.addAlias("file_centric", list);

    assertThat(added).isTrue();

    val state = repository.getAliasState();
    list.forEach(index -> {
      val indexState = state.get(index);
      assertThat(indexState).isNotEmpty();
      assertThat(indexState.get(0).alias()).isEqualTo("file_centric");
    });

    val removed = repository.removeAlias("file_centric", Lists.list(INDEX1,INDEX2,INDEX3));
    assertThat(removed).isTrue();
    repository.getAliasState().valuesIt().forEachRemaining(i -> assertThat(i).isEmpty());
  }

}