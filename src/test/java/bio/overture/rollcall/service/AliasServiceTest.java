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
import bio.overture.rollcall.exception.ReleaseIntegrityException;
import bio.overture.rollcall.model.AliasRequest;
import bio.overture.rollcall.repository.IndexRepository;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.HttpHost;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AliasServiceTest {

  @ClassRule
  public static GenericContainer esContainer =
    new FixedHostPortGenericContainer("docker.elastic.co/elasticsearch/elasticsearch:7.5.2")
      .withFixedExposedPort(10200, 9200)
      .withFixedExposedPort(10300, 9300)
      .waitingFor(Wait.forHttp("/")) // Wait until elastic start
      .withEnv("discovery.type", "single-node");

  private static String INDEX1 = "file_centric_sd_ygva0e1c_re_foobar1";
  private static String INDEX2 = "file_centric_sd_preasa7s_re_foobar1";
  private static String INDEX3 = "file_centric_sd_preasa7s_re_foobar2";

  private RestHighLevelClient client;
  private IndexRepository repository;
  private AliasService service;

  @Before
  @SneakyThrows
  public void setUp() {
    client = new RestHighLevelClient( RestClient.builder(new HttpHost(InetAddress.getByName(esContainer.getContainerIpAddress()), 10200)));
    repository = new IndexRepository(client);

    val config = new RollcallConfig(Lists.list(
            new RollcallConfig.ConfiguredAlias("file_centric", "file", "centric", 1),
            new RollcallConfig.ConfiguredAlias("participant_centric", "participant", "centric")
    ));
    service = new AliasService(config, repository);

    client.indices().create(new CreateIndexRequest(INDEX1), RequestOptions.DEFAULT);
    client.indices().create(new CreateIndexRequest(INDEX2), RequestOptions.DEFAULT);
    client.indices().create(new CreateIndexRequest(INDEX3), RequestOptions.DEFAULT);
    client.indices().create(new CreateIndexRequest("badindex"), RequestOptions.DEFAULT);
  }

  @After
  @SneakyThrows
  public void tearDown() {
    // delete all indices
    client.indices().delete(new DeleteIndexRequest("*"), RequestOptions.DEFAULT);
  }

  @Test
  public void getConfiguredTest() {
    val configured = service.getConfigured();
    assertThat(configured).hasSize(1);
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

  @Test
  public void testReleaseNonDestructiveFailurePreFlight() {
    val request1 = new AliasRequest("file_centric", "RE_foobar1", Lists.list("SD_preasa7s", "sd_ygva0e1c"));
    service.release(request1);
    val state1 = repository.getAliasState();
    assertThat(state1.get(INDEX1).get(0).alias()).isEqualTo("file_centric");
    assertThat(state1.get(INDEX2).get(0).alias()).isEqualTo("file_centric");

    val badRequest = new AliasRequest("file_centric", "THIS_RELEASE_DONT_EXIST", Lists.list("SD_preasa7s", "sd_ygva0e1c"));
    assertThatThrownBy(() -> service.release(badRequest)).isInstanceOf(ReleaseIntegrityException.class);

    // Should not have changed.
    val state2 = repository.getAliasState();
    assertThat(state2.get(INDEX1).get(0).alias()).isEqualTo("file_centric");
    assertThat(state2.get(INDEX2).get(0).alias()).isEqualTo("file_centric");
  }

  @Test
  @SneakyThrows
  public void testReleaseAndDeleteOldIndices() {
    // release foobar2
    val request1 = new AliasRequest("file_centric", "RE_foobar2", Lists.list( "sd_preasa7s"));
    service.release(request1);

    // verify aliases assigned to indices
    val state1 = repository.getAliasState();
    assertThat(state1.get(INDEX2).isEmpty()).isTrue();
    assertThat(state1.get(INDEX3).get(0).alias()).isEqualTo("file_centric");

    // add new index and assert current indices
    final String INDEX4 = "file_centric_sd_preasa7s_re_foobar3";
    client.indices().create(new CreateIndexRequest(INDEX4), RequestOptions.DEFAULT);
    val indicesBeforeRelease = repository.getIndices();
    assertThat(indicesBeforeRelease).containsExactlyInAnyOrder(INDEX1, INDEX2, INDEX3, "badindex", INDEX4);

    // release foobar3
    val request2 = new AliasRequest("file_centric", "RE_foobar3", Lists.list("SD_preasa7s"));
    service.release(request2);

    // verify aliases assigned to indices
    val state2 = repository.getAliasState();
    assertThat(state2.get(INDEX2)).isNull(); // sd_preasa7s foobar3 release deleted old foobar1 since keeping `1` latestNonReleasedIndex
    assertThat(state2.get(INDEX3).isEmpty()).isTrue(); // sd_preasa7s unreleased index foobar2 is kept
    assertThat(state2.get(INDEX4).get(0).alias()).isEqualTo("file_centric"); // new released index

    // assert current indices
    val indicesAfterRelease = repository.getIndices();
    assertThat(indicesAfterRelease).containsExactlyInAnyOrder(INDEX1, INDEX3, "badindex", INDEX4);
  }

  @Test
  @SneakyThrows
  public void testIndicesNotDeletedIfNotConfigured() {
    final String INDEXA = "participant_centric_sd_preasa7s_re_foobar1";
    final String INDEXB = "participant_centric_sd_preasa7s_re_foobar2";
    final String INDEXC = "participant_centric_sd_preasa7s_re_foobar3";

    client.indices().create(new CreateIndexRequest(INDEXA), RequestOptions.DEFAULT);
    client.indices().create(new CreateIndexRequest(INDEXB), RequestOptions.DEFAULT);
    client.indices().create(new CreateIndexRequest(INDEXC), RequestOptions.DEFAULT);

    val indicesBeforeRelease = repository.getIndices();
    assertThat(indicesBeforeRelease).containsExactlyInAnyOrder(INDEX1, INDEX2, INDEX3, "badindex", INDEXA, INDEXB, INDEXC);

    // release foobar3 in participant_centric, shouldn't delete any participant_centric index since it was not configured
    val request = new AliasRequest("participant_centric", "RE_foobar3", Lists.list( "sd_preasa7s"));
    service.release(request);

    // assert current indices
    val indicesAfterRelease = repository.getIndices();
    assertThat(indicesAfterRelease).containsExactlyInAnyOrder(indicesBeforeRelease);
  }
}