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

package bio.overture.rollcall.index;

import bio.overture.rollcall.antlr4.IndexNameLexer;
import bio.overture.rollcall.antlr4.IndexNameParser;
import lombok.SneakyThrows;
import lombok.val;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class IndexParser {

  @SneakyThrows
  public static ResolvedIndex parse(String indexName) {
    val result = anltrParse(indexName);
    val part = result.indexName().accept(new IndexNameVisitor());
    return reduceToResolved(part);
  }

  private static IndexNameParser anltrParse(String indexName) {
    val chars = CharStreams.fromString(indexName);
    val lexer = new IndexNameLexer(chars);
    val tokens = new CommonTokenStream(lexer);
    return new IndexNameParser(tokens);
  }

  private static ResolvedIndex reduceToResolved(IndexNamePart part) {
    val resolved = ResolvedIndex.builder();
    reduceToResolved(part, resolved);
    part.children.forEach(p -> reduceToResolved(p, resolved));
    return resolved.build();
  }

  private static void reduceToResolved(IndexNamePart part, ResolvedIndex.ResolvedIndexBuilder builder) {
    switch (part.part) {
      case INDEX:
        builder.indexName(part.getValue());
        break;
      case ENTITY:
        builder.entity(part.getValue());
        break;
      case TYPE:
        builder.type(part.getValue());
        break;
      case SHARD_PREFIX:
        builder.shardPrefix(part.getValue());
        break;
      case SHARD:
        builder.shard(part.getValue());
        break;
      case RELEASE_PREFIX:
        builder.releasePrefix(part.getValue());
        break;
      case RELEASE:
        builder.release(part.getValue());
        break;
      default:
        throw new UnsupportedOperationException("Unsupported index name partition. Could not resolve.");
    }
  }

}
