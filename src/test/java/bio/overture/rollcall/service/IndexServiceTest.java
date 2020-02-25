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
        client = new RestHighLevelClient( RestClient.builder(new HttpHost(InetAddress.getByName(esContainer.getIpAddress()), 10200)));
        repository = new IndexRepository(client);

        val config = new RollcallConfig(Lists.list(new RollcallConfig.ConfiguredAlias("file_centric", "file", "centric")));
        aliasService = new AliasService(config, repository);
        service = new IndexService(repository, aliasService);
    }

    @After
    @SneakyThrows
    public void tearDown() {
        client.indices().delete(new DeleteIndexRequest("*"), RequestOptions.DEFAULT);
    }

    @Test
    public void testIndexCreationAndUpdatedRelease() {
        // start with an index
        val START_INDEX = "file_centric_sd_kkde23_re_foobar1" ;
        repository.createIndex(START_INDEX);

        // create new index, an index with the parameters already exists so it should create index with updated release value
        val req1 = new CreateResolvableIndexRequest("file", "centric", "sd", "kkde23", "re", false, null);
        val newResolvedIndex = service.createResolvableIndex(req1);
        assertThat(newResolvedIndex.getIndexName()).isEqualTo("file_centric_sd_kkde23_re_2"); // previous release had major int 1, so bump to 2

        val candidiates = aliasService.getRelevantCandidates("file_centric");
        assertThat(candidiates.getIndices().size()).isEqualTo(2);
        assertThat(candidiates.getIndices())
                .extracting(i -> i.getRelease())
                .containsExactlyInAnyOrder("foobar1",  "2");


        // create new index, two index with the parameters already exists so it should create a third index with updated release value
        val anotherNewResolvedIndex = service.createResolvableIndex(req1);
        assertThat(anotherNewResolvedIndex.getIndexName()).isEqualTo("file_centric_sd_kkde23_re_3"); // previous release had major int 2, so bump to 3

        val candidiatesAfterSecondAdd = aliasService.getRelevantCandidates("file_centric");
        assertThat(candidiatesAfterSecondAdd.getIndices().size()).isEqualTo(3);
        assertThat(candidiatesAfterSecondAdd.getIndices())
                .extracting(i -> i.getRelease())
                .containsExactlyInAnyOrder("foobar1",  "2", "3");
    }

    @Test
    public void testIndexCreationAndUpdatedReleaseValuWithMultiplePart() {
        // start with one index
        repository.createIndex("file_centric_sd_kkde23_re_v1b1");

        // create new index, a similar index already exists so it should create new index with updated release value
        val req1 = new CreateResolvableIndexRequest("file", "centric", "sd", "kkde23", "re", false, null);
        val newIndexName = service.createResolvableIndex(req1).getIndexName();
        assertThat(newIndexName).isEqualTo("file_centric_sd_kkde23_re_1"); // major release 1 didn't exist so it made one

        val candidiates = aliasService.getRelevantCandidates("file_centric");
        assertThat(candidiates.getIndices().size()).isEqualTo(2);
        assertThat(candidiates.getIndices())
                .extracting(i -> i.getRelease())
                .containsExactlyInAnyOrder("v1b1",  "1");

        // add a third index
        repository.createIndex("file_centric_sd_kkde23_re_v1b2");

        // create new index, three index with same parameters already exists so it should create a fourth index with updated release value
        val newIndexAfterBetaTwo = service.createResolvableIndex(req1).getIndexName();
        assertThat(newIndexAfterBetaTwo).isEqualTo("file_centric_sd_kkde23_re_2");

        val candidatesAfterSecondAdd = aliasService.getRelevantCandidates("file_centric");
        assertThat(candidatesAfterSecondAdd.getIndices().size()).isEqualTo(4);
        assertThat(candidatesAfterSecondAdd.getIndices())
                .extracting(i -> i.getRelease())
                .containsExactlyInAnyOrder("v1b1", "v1b2", "1", "2");
    }

    @Test
    @SneakyThrows
    public void testIndexCloneAndUpdatedRelease() {
        // start with an index with one document in it
        val originalIndexName = "file_centric_sd_kkde23_re_1";
        repository.createIndex(originalIndexName);
        client.index(new IndexRequest(originalIndexName).id("1").source("{ \"testKey\": \"testVal\" }", XContentType.JSON), RequestOptions.DEFAULT);

        // release the existing index
        aliasService.release(new AliasRequest("file_centric", "re_1", Lists.list("sd_kkde23")));

        // create new index, this request is asking to clone, so it should clone existing released index and update release value
        val req1 = new CreateResolvableIndexRequest("file", "centric", "sd" , "kkde23", "re", true, null);
        val newIndexName = service.createResolvableIndex(req1).getIndexName();
        assertThat(newIndexName).isEqualTo("file_centric_sd_kkde23_re_2");

        val candidates = aliasService.getRelevantCandidates("file_centric");
        assertThat(candidates.getIndices().size()).isEqualTo(2);
        assertThat(candidates.getIndices()).extracting(i -> i.getRelease()).contains("1",  "2");

        val document1 = client.get(new GetRequest(originalIndexName, "1"), RequestOptions.DEFAULT).getSourceAsMap();
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
        final List<ResolvedIndex> resolvedIndices = Arrays.stream(dummyIndexNames).map(name -> IndexParser.parse(name)).collect(Collectors.toList());

        // list has resolved indices in generated order
        assertThat(resolvedIndices.stream().map(ri -> ri.getIndexName())).containsExactly(dummyIndexNames);

        // sort collection of resolved indices
        resolvedIndices.sort(IndexService.ResolvedIndexByReleaseComparator);

        // assert expected order of list after sorting, order is oldest to latest
        assertThat(resolvedIndices.stream().map(ri -> ri.getIndexName())).containsExactly(
                "file_centric_sd_kkde23_re_1",
                "file_centric_sd_kkde23_re_2",
                "file_centric_sd_kkde23_re_2",
                "file_centric_sd_kkde23_re_3beta1",
                "file_centric_sd_kkde23_re_3beta1",
                "file_centric_sd_kkde23_re_3beta2",
                "file_centric_sd_kkde23_re_3"
        );
    }
}
