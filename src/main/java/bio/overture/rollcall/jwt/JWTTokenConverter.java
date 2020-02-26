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

import bio.overture.rollcall.util.TypeUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Map;

@Slf4j
public class JWTTokenConverter extends JwtAccessTokenConverter {

  public JWTTokenConverter(String publicKey) {
    super();
    this.setVerifierKey(publicKey);
  }

  @Override
  public OAuth2Authentication extractAuthentication(@NonNull Map<String, ?> map) {
    OAuth2Authentication authentication = super.extractAuthentication(map);

    val context = (Map<String, ?>) map.get("context");
    val user = (Map<String, ?>) context.get("user");
    val jwtUser = TypeUtils.convertType(user, JWTUser.class);

    // Additional Logic for getting an ego userId.
    if (map.containsKey("sub")) {
      jwtUser.setUid(map.get("sub").toString());
    } else {
      throw new UnauthorizedUserException("bad token");
    }

    authentication.setDetails(jwtUser);

    return authentication;
  }

}
