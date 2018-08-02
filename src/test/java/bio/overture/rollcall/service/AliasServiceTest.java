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

package bio.overture.rollcall.service;

import bio.overture.rollcall.config.RollcallConfig;
import bio.overture.rollcall.model.AliasRequest;
import bio.overture.rollcall.repository.IndexRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

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
  private IndexRepository repository;
  private AliasService service;

  @Before
  @SneakyThrows
  public void setUp() {
    client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", "docker-cluster").build())
      .addTransportAddress(new TransportAddress(InetAddress.getByName(esContainer.getIpAddress()), 10300));
    repository = new IndexRepository(client);

    val config = new RollcallConfig(Lists.list(new RollcallConfig.ConfiguredAlias("file_centric", "file", "centric")));
    service = new AliasService(config, repository);

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
    val state1 = repository.getAliasState();
    assertThat(state1.get(INDEX1).get(0).alias()).isEqualTo("file_centric");
    assertThat(state1.get(INDEX2).get(0).alias()).isEqualTo("file_centric");

    val request2 = new AliasRequest("file_centric", "RE_foobar2", Lists.list("SD_preasa7s"));
    service.release(request2);
    val state2 = repository.getAliasState();
    assertThat(state2.get(INDEX1).get(0).alias()).isEqualTo("file_centric");
    assertThat(state2.get(INDEX2).isEmpty()).isTrue();
    assertThat(state2.get(INDEX3).get(0).alias()).isEqualTo("file_centric");
  }

}