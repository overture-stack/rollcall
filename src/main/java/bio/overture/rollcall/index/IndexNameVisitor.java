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
