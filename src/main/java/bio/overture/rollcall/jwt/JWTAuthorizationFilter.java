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

package bio.overture.rollcall.jwt;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@Slf4j
public class JWTAuthorizationFilter extends GenericFilterBean {

  private final static String ADMIN_ROLE = "ADMIN";
  private final static String APPROVED_STATUS = "Approved";

  @Override
  @SneakyThrows
  public void doFilter(@NonNull ServletRequest request, @NonNull ServletResponse response, @NonNull FilterChain chain) {
    val authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {

      val details = (OAuth2AuthenticationDetails) authentication.getDetails();
      val user = (JWTUser) details.getDecodedDetails();

      boolean hasCorrectRole = user.getRoles().contains(ADMIN_ROLE);
      boolean hasCorrectStatus = user.getStatus().equalsIgnoreCase(APPROVED_STATUS);

      if (!hasCorrectRole || !hasCorrectStatus) {
        SecurityContextHolder.clearContext();
      }
    }

    chain.doFilter(request, response);
  }

}
