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

package bio.overture.rollcall.exception;

import bio.overture.rollcall.model.ErrorResponse;
import lombok.NonNull;
import lombok.val;
import org.elasticsearch.ElasticsearchException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
public class RollcallExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(ReleaseIntegrityException.class)
  public ResponseEntity<ErrorResponse> handleRelease(@NonNull ReleaseIntegrityException ex) {
    val error = new ErrorResponse(
      CONFLICT.value(),
      CONFLICT,
      "Release integrity violation.",
      of(
        "releaseId", ex.getReleaseId(),
        "shardIds", ex.getShardIds(),
        "resolvedIndices", ex.getResolvedIndices()));

    return new ResponseEntity<>(error, CONFLICT);
  }

  @ExceptionHandler(NoSuchAliasWithCandidatesException.class)
  public ResponseEntity<ErrorResponse> handleNoAlias(@NonNull NoSuchAliasWithCandidatesException ex) {
    val error = new ErrorResponse(
      NOT_FOUND.value(),
      NOT_FOUND,
      ex.getMessage(),
      emptyMap());

    return new ResponseEntity<>(error, NOT_FOUND);
  }

  @ExceptionHandler(ElasticsearchException.class)
  public ResponseEntity<ErrorResponse> handleElasticSearchError(@NonNull ElasticsearchException ex) {
    val error = new ErrorResponse(
            INTERNAL_SERVER_ERROR.value(),
            INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            emptyMap());

    return new ResponseEntity<>(error, INTERNAL_SERVER_ERROR);
  }
}
