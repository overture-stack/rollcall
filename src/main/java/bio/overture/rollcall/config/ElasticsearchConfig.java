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

package bio.overture.rollcall.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
@Slf4j
public class ElasticsearchConfig {

  @Value("${elasticsearch.node}")
  private String node;

  @Value("${elasticsearch.authEnabled:false}")
  private boolean authEnabled;

  @Value("${elasticsearch.trustSelfSignedCert:true}")
  private boolean trustSelfSignedCert;

  @Value("${elasticsearch.user}")
  private String user;

  @Value("${elasticsearch.password}")
  private String password;

  @Bean
  @SneakyThrows
  public RestHighLevelClient restClient() {
      val builder = RestClient.builder(HttpHost.create(node));

      builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {
          if (trustSelfSignedCert) {
              log.debug("Elasticsearch Client - trustSelfSignedCert enabled so setting SSLContext");
              SSLContextBuilder sslCtxBuilder = new SSLContextBuilder();
              try {
                  sslCtxBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                  httpAsyncClientBuilder.setSSLContext(sslCtxBuilder.build());
                  httpAsyncClientBuilder.setSSLHostnameVerifier((s, sslSession) -> true); // this is for local only
              } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                  throw new RuntimeException("failed to build Elastic rest client");
              }
          }

          if (authEnabled) {
              log.debug("Elasticsearch Client - authEnabled enabled so setting credentials provider");
              BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
              credentialsProvider.setCredentials(AuthScope.ANY,
                      new UsernamePasswordCredentials(user, password));
              httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
          }

          return httpAsyncClientBuilder;
      });

    log.info("Elasticsearch Client - built");

    return new RestHighLevelClient(builder);
  }
}
