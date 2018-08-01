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

import bio.overture.rollcall.index.ResolvedIndex.Part;
import bio.overture.rollcall.antlr4.IndexNameBaseVisitor;
import bio.overture.rollcall.antlr4.IndexNameParser.*;
import bio.overture.rollcall.antlr4.IndexNameParser.IndexNameContext;
import lombok.val;

import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class IndexNameVisitor extends IndexNameBaseVisitor<IndexNamePart> {

  @Override
  public IndexNamePart visitIndexName(IndexNameContext ctx) {
    val result = ctx.children.stream()
      .map(child -> child.accept(this))
      .filter(Objects::nonNull)
      .collect(toList());

    return new IndexNamePart(Part.INDEX, ctx.getText(), result);
  }

  @Override
  public IndexNamePart visitEntity(EntityContext ctx) {
    return new IndexNamePart(Part.ENTITY, ctx.getChild(0).getText());
  }

  @Override public IndexNamePart visitPart(PartContext ctx) {
    return new IndexNamePart(Part.TYPE, ctx.getChild(0).getText());
  }

  @Override public IndexNamePart visitShardPrefix(ShardPrefixContext ctx) {
    return new IndexNamePart(Part.SHARD_PREFIX, ctx.getChild(0).getText());
  }

  @Override public IndexNamePart visitShard(ShardContext ctx) {
    return new IndexNamePart(Part.SHARD, ctx.getChild(0).getText());
  }

  @Override public IndexNamePart visitReleasePrefix(ReleasePrefixContext ctx) {
    return new IndexNamePart(Part.RELEASE_PREFIX, ctx.getChild(0).getText());
  }

  @Override public IndexNamePart visitRelease(ReleaseContext ctx) {
    return new IndexNamePart(Part.RELEASE, ctx.getChild(0).getText());
  }

}
