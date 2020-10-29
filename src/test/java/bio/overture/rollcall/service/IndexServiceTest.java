/*
 * Copyright (c) 2020. The Ontario Institute for Cancer Research. All rights reserved.
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
import bio.overture.rollcall.index.IndexParser;
import bio.overture.rollcall.index.ResolvedIndex;
import bio.overture.rollcall.model.AliasRequest;
import bio.overture.rollcall.model.CreateResolvableIndexRequest;
import bio.overture.rollcall.repository.IndexRepository;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.HttpHost;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.*;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexServiceTest {
    private static final String ALIAS_NAME = "files";
    private static final String ENTITY_VALUE = "file";
    private static final String TYPE_VALUE = "centric";
    private static final String INDEX_NAME_WITH_NO_RELEASE = "file_centric_sd_kkde23_re_";
    private static final String EXISTING_INDEX_RELEASE_VALUE = "foobar1";
    private static final String EXISTING_INDEX = INDEX_NAME_WITH_NO_RELEASE + EXISTING_INDEX_RELEASE_VALUE;

    @ClassRule
    public static GenericContainer esContainer =
            new FixedHostPortGenericContainer("docker.elastic.co/elasticsearch/elasticsearch:7.5.2")
                    .withFixedExposedPort(10200, 9200)
                    .withFixedExposedPort(10300, 9300)
                    .waitingFor(Wait.forHttp("/")) // Wait until elastic start
                    .withEnv("discovery.type", "single-node");

    private RestHighLevelClient client;
    private IndexRepository repository;
    private AliasService aliasService;
    private IndexService service;

    @Before
    @SneakyThrows
    public void setUp() {
        client = new RestHighLevelClient( RestClient.builder(new HttpHost(InetAddress.getByName(esContainer.getContainerIpAddress()), 10200)));
        repository = new IndexRepository(client);

        val config = new RollcallConfig(Lists.list(new RollcallConfig.ConfiguredAlias(ALIAS_NAME, ENTITY_VALUE, TYPE_VALUE)));
        aliasService = new AliasService(config, repository);
        service = new IndexService(config, repository);

        repository.createIndex(EXISTING_INDEX);
    }

    @After
    @SneakyThrows
    public void tearDown() {
        client.indices().delete(new DeleteIndexRequest("*"), RequestOptions.DEFAULT);
    }

    @Test
    @SneakyThrows
    public void testIndexSettingOnCreateIndex() {
        val indexSetting = "{\"index.number_of_shards\":3,\"index.number_of_replicas\":2}";
        val req = new CreateResolvableIndexRequest(ENTITY_VALUE, TYPE_VALUE, "sd", "kkde23", "re", false, indexSetting);
        val newResolvedIndex = service.createResolvableIndex(req);
        val getSettingsReq = new GetSettingsRequest().indices(newResolvedIndex.getIndexName());
        val resp = client.indices().getSettings(getSettingsReq, RequestOptions.DEFAULT);
        val replicasNum = Integer.valueOf(resp.getSetting(newResolvedIndex.getIndexName(), "index.number_of_replicas"));
        val shardNum = Integer.valueOf(resp.getSetting(newResolvedIndex.getIndexName(), "index.number_of_shards"));
        assertThat(shardNum).isEqualTo(3);
        assertThat(replicasNum).isEqualTo(2);
    }

    @Test
    public void testIndexCreationAndUpdatedRelease() {
        // create new index, an index with the parameters already exists so it should create index with updated release value
        val createResolvableIndexReq = makeNewCreateRequest(false);
        val newResolvedIndex = service.createResolvableIndex(createResolvableIndexReq);
        assertThat(newResolvedIndex.getIndexName()).isEqualTo(INDEX_NAME_WITH_NO_RELEASE + "2");

        val indicesAfterFirstCreate = aliasService.getRelevantCandidates(ALIAS_NAME).getIndices();
        assertThat(indicesAfterFirstCreate.size()).isEqualTo(2);
        assertThat(indicesAfterFirstCreate)
                .extracting(ResolvedIndex::getRelease)
                .containsExactlyInAnyOrder(EXISTING_INDEX_RELEASE_VALUE, "2");

        // create new index, two index with the parameters already exists so it should create a third index with updated release value
        val anotherNewResolvedIndex = service.createResolvableIndex(createResolvableIndexReq);
        // previous release had major int 2, so bump to 3
        assertThat(anotherNewResolvedIndex.getIndexName()).isEqualTo(INDEX_NAME_WITH_NO_RELEASE + "3");

        val indicesAfterSecondCreate = aliasService.getRelevantCandidates(ALIAS_NAME).getIndices();
        assertThat(indicesAfterSecondCreate.size()).isEqualTo(3);
        assertThat(indicesAfterSecondCreate)
                .extracting(ResolvedIndex::getRelease)
                .containsExactlyInAnyOrder(EXISTING_INDEX_RELEASE_VALUE, "2", "3");
    }

    @Test
    public void testIndexCreationAndUpdatedReleaseValuWithMultiplePart() {
        repository.createIndex(INDEX_NAME_WITH_NO_RELEASE + "v2b1"); // add a beta index for version 2

        val req1 = this.makeNewCreateRequest(false);
        val newIndexName = service.createResolvableIndex(req1).getIndexName();
        assertThat(newIndexName).isEqualTo(INDEX_NAME_WITH_NO_RELEASE + "2"); // major release 2 didn't exist so made one

        val relevantIndices = aliasService.getRelevantCandidates(ALIAS_NAME).getIndices();
        assertThat(relevantIndices.size()).isEqualTo(3);
        assertThat(relevantIndices)
                .extracting(ResolvedIndex::getRelease)
                .contains("v2b1",  "2");

        repository.createIndex(INDEX_NAME_WITH_NO_RELEASE + "v2b2"); // add second version 2 beta index

        // create new index, since index with major release exists it should increment that and ignore the second beta release
        val newIndexAfterBetaTwo = service.createResolvableIndex(req1).getIndexName();
        assertThat(newIndexAfterBetaTwo).isEqualTo(INDEX_NAME_WITH_NO_RELEASE + "3");

        val indicesAfterSecondAdd = aliasService.getRelevantCandidates(ALIAS_NAME).getIndices();
        assertThat(indicesAfterSecondAdd.size()).isEqualTo(5);
        assertThat(indicesAfterSecondAdd)
                .extracting(ResolvedIndex::getRelease)
                .contains("v2b1", "v2b2", "2", "3");
    }

    @Test
    @SneakyThrows
    public void testIndexCloneAndUpdatedRelease() {
        // add a document to existing index
        client.index(new IndexRequest(EXISTING_INDEX).id("1").source("{ \"testKey\": \"testVal\" }", XContentType.JSON), RequestOptions.DEFAULT);

        this.releaseIndex(EXISTING_INDEX);

        // clone index, it should clone existing released index and update release value
        val req = this.makeNewCreateRequest(true);
        val newIndexName = service.createResolvableIndex(req).getIndexName();

        assertThat(newIndexName).isEqualTo("file_centric_sd_kkde23_re_2");

        val indices = aliasService.getRelevantCandidates(ALIAS_NAME).getIndices();
        assertThat(indices.size()).isEqualTo(2);
        assertThat(indices)
                .extracting(ResolvedIndex::getRelease)
                .contains(EXISTING_INDEX_RELEASE_VALUE,  "2");

        val document1 = client.get(new GetRequest(EXISTING_INDEX, "1"), RequestOptions.DEFAULT).getSourceAsMap();
        val document2 = client.get(new GetRequest(newIndexName, "1"), RequestOptions.DEFAULT).getSourceAsMap();

        // clone so both documents should have same fields
        assertThat(document1).isEqualTo(document2);
    }

    @Test
    @SneakyThrows
    public void testResolvedIndexByReleaseComparator() {
        final String[] dummyIndexNames = {
                "file_centric_sd_kkde23_re_2",
                "file_centric_sd_kkde23_re_1",
                "file_centric_sd_kkde23_re_2",
                "file_centric_sd_kkde23_re_3",
                "file_centric_sd_kkde23_re_3beta1",
                "file_centric_sd_kkde23_re_3beta2",
                "file_centric_sd_kkde23_re_3beta1",
        };
        final List<ResolvedIndex> resolvedIndices = Arrays.stream(dummyIndexNames).map(IndexParser::parse).collect(Collectors.toList());

        // list has resolved indices in generated order
        assertThat(resolvedIndices.stream().map(ResolvedIndex::getIndexName)).containsExactly(dummyIndexNames);

        // sort collection of resolved indices
        resolvedIndices.sort(IndexService.ResolvedIndexByReleaseComparator);

        // assert expected order of list after sorting, order is oldest to latest
        assertThat(resolvedIndices.stream().map(ResolvedIndex::getIndexName)).containsExactly(
                "file_centric_sd_kkde23_re_1",
                "file_centric_sd_kkde23_re_2",
                "file_centric_sd_kkde23_re_2",
                "file_centric_sd_kkde23_re_3beta1",
                "file_centric_sd_kkde23_re_3beta1",
                "file_centric_sd_kkde23_re_3beta2",
                "file_centric_sd_kkde23_re_3"
        );
    }

    private void releaseIndex(String indexName) {
        val indexNameParts = indexName.split("_");
        val shard = indexNameParts[2] + "_" + indexNameParts[3];
        val release = indexNameParts[4] + "_" + indexNameParts[5];

        aliasService.release(new AliasRequest(ALIAS_NAME, release, Lists.list(shard)));
    }

    private CreateResolvableIndexRequest makeNewCreateRequest(Boolean clonePreviousReleased) {
        return new CreateResolvableIndexRequest(ENTITY_VALUE, TYPE_VALUE, "sd", "kkde23", "re", clonePreviousReleased, null);
    }
}
