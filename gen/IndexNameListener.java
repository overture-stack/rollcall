// Generated from /home/dandric/Documents/workspace/rollcall/src/main/antlr4/bio/overture/rollcall/IndexName.g4 by ANTLR 4.7
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IndexNameParser}.
 */
public interface IndexNameListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IndexNameParser#indexName}.
	 * @param ctx the parse tree
	 */
	void enterIndexName(IndexNameParser.IndexNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link IndexNameParser#indexName}.
	 * @param ctx the parse tree
	 */
	void exitIndexName(IndexNameParser.IndexNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link IndexNameParser#entity}.
	 * @param ctx the parse tree
	 */
	void enterEntity(IndexNameParser.EntityContext ctx);
	/**
	 * Exit a parse tree produced by {@link IndexNameParser#entity}.
	 * @param ctx the parse tree
	 */
	void exitEntity(IndexNameParser.EntityContext ctx);
	/**
	 * Enter a parse tree produced by {@link IndexNameParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(IndexNameParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IndexNameParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(IndexNameParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IndexNameParser#shardPrefix}.
	 * @param ctx the parse tree
	 */
	void enterShardPrefix(IndexNameParser.ShardPrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link IndexNameParser#shardPrefix}.
	 * @param ctx the parse tree
	 */
	void exitShardPrefix(IndexNameParser.ShardPrefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link IndexNameParser#shard}.
	 * @param ctx the parse tree
	 */
	void enterShard(IndexNameParser.ShardContext ctx);
	/**
	 * Exit a parse tree produced by {@link IndexNameParser#shard}.
	 * @param ctx the parse tree
	 */
	void exitShard(IndexNameParser.ShardContext ctx);
	/**
	 * Enter a parse tree produced by {@link IndexNameParser#release}.
	 * @param ctx the parse tree
	 */
	void enterRelease(IndexNameParser.ReleaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link IndexNameParser#release}.
	 * @param ctx the parse tree
	 */
	void exitRelease(IndexNameParser.ReleaseContext ctx);
}