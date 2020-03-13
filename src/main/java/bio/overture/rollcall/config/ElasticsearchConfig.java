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

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
@ConfigurationProperties("elasticsearch")
public class ElasticsearchConfig {

  @Value("${elasticsearch.host}")
  @NonNull private String host;

  @Value("${elasticsearch.port}")
  @NonNull private int port;

  @Value("${elasticsearch.authEnabled}")
  @NonNull private boolean authEnabled;

  @Value("${elasticsearch.user}")
  @NonNull private String user;

  @Value("${elasticsearch.password}")
  @NonNull private String password;

  @Bean
  @SneakyThrows
  public RestHighLevelClient restClient() {
      val builder = RestClient.builder(new HttpHost(new URL(host).getHost(), port));
      if (authEnabled) {
          builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {
              val credentialsProvider = new BasicCredentialsProvider();
              credentialsProvider.setCredentials(AuthScope.ANY,
                      new UsernamePasswordCredentials(user, password));
              return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
          });
      }
      return new RestHighLevelClient(builder);
  }
}
