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

package bio.overture.rollcall.controller;

import bio.overture.rollcall.config.RollcallConfig.ConfiguredAlias;
import bio.overture.rollcall.model.AliasRequest;
import bio.overture.rollcall.service.AliasService;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/aliases")
public class AliasController {

  private final AliasService service;

  public AliasController(@NonNull AliasService service) {
    this.service = service;
  }

  @GetMapping("config")
  public List<ConfiguredAlias> getConfigured() {
    return service.getConfigured();
  }

  @GetMapping("candidates")
  public List<AliasService.AliasCandidates> getCandidates() {
    return service.getCandidates();
  }

  @PostMapping(path = "release", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public boolean release(@RequestBody AliasRequest aliasRequest) {
    return service.release(aliasRequest);
  }

  @PostMapping(path = "remove", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public boolean remove(@RequestBody AliasRequest aliasRequest) {
    return service.remove(aliasRequest);
  }

}
