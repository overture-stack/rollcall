// Generated from /home/dandric/Documents/workspace/rollcall/src/main/antlr4/bio/overture/rollcall/IndexName.g4 by ANTLR 4.7
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link IndexNameParser}.
 *
 * @param <T> The return part of the visit operation. Use {@link Void} for
 * operations with no return part.
 */
public interface IndexNameVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link IndexNameParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(IndexNameParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link IndexNameParser#entity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEntity(IndexNameParser.EntityContext ctx);
	/**
	 * Visit a parse tree produced by {@link IndexNameParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(IndexNameParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link IndexNameParser#shardPrefix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShardPrefix(IndexNameParser.ShardPrefixContext ctx);
	/**
	 * Visit a parse tree produced by {@link IndexNameParser#shard}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShard(IndexNameParser.ShardContext ctx);
	/**
	 * Visit a parse tree produced by {@link IndexNameParser#release}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelease(IndexNameParser.ReleaseContext ctx);
}