// Generated from /home/dandric/Documents/workspace/rollcall/src/main/antlr4/bio/overture/rollcall/IndexName.g4 by ANTLR 4.7
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class IndexNameParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, ALPHANUM=4, SEP=5, WS=6;
	public static final int
		RULE_indexName = 0, RULE_entity = 1, RULE_type = 2, RULE_shardPrefix = 3, 
		RULE_shard = 4, RULE_release = 5;
	public static final String[] ruleNames = {
		"indexName", "entity", "part", "shardPrefix", "shard", "release"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'centric'", "'text'", "'entity'", null, "'_'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, "ALPHANUM", "SEP", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "IndexName.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public IndexNameParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class IndexNameContext extends ParserRuleContext {
		public EntityContext entity() {
			return getRuleContext(EntityContext.class,0);
		}
		public List<TerminalNode> SEP() { return getTokens(IndexNameParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(IndexNameParser.SEP, i);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public ShardPrefixContext shardPrefix() {
			return getRuleContext(ShardPrefixContext.class,0);
		}
		public ShardContext shard() {
			return getRuleContext(ShardContext.class,0);
		}
		public ReleaseContext release() {
			return getRuleContext(ReleaseContext.class,0);
		}
		public IndexNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).enterIndexName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).exitIndexName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IndexNameVisitor ) return ((IndexNameVisitor<? extends T>)visitor).visitIndexName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexNameContext indexName() throws RecognitionException {
		IndexNameContext _localctx = new IndexNameContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_indexName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(12);
			entity();
			setState(13);
			match(SEP);
			setState(14);
			type();
			setState(15);
			match(SEP);
			setState(16);
			shardPrefix();
			setState(17);
			match(SEP);
			setState(18);
			shard();
			setState(19);
			match(SEP);
			setState(20);
			release();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EntityContext extends ParserRuleContext {
		public TerminalNode ALPHANUM() { return getToken(IndexNameParser.ALPHANUM, 0); }
		public EntityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entity; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).enterEntity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).exitEntity(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IndexNameVisitor ) return ((IndexNameVisitor<? extends T>)visitor).visitEntity(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EntityContext entity() throws RecognitionException {
		EntityContext _localctx = new EntityContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_entity);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22);
			match(ALPHANUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeContext extends ParserRuleContext {
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).exitType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IndexNameVisitor ) return ((IndexNameVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(24);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShardPrefixContext extends ParserRuleContext {
		public TerminalNode ALPHANUM() { return getToken(IndexNameParser.ALPHANUM, 0); }
		public ShardPrefixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shardPrefix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).enterShardPrefix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).exitShardPrefix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IndexNameVisitor ) return ((IndexNameVisitor<? extends T>)visitor).visitShardPrefix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShardPrefixContext shardPrefix() throws RecognitionException {
		ShardPrefixContext _localctx = new ShardPrefixContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_shardPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(26);
			match(ALPHANUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShardContext extends ParserRuleContext {
		public TerminalNode ALPHANUM() { return getToken(IndexNameParser.ALPHANUM, 0); }
		public ShardContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shard; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).enterShard(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).exitShard(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IndexNameVisitor ) return ((IndexNameVisitor<? extends T>)visitor).visitShard(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShardContext shard() throws RecognitionException {
		ShardContext _localctx = new ShardContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_shard);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			match(ALPHANUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReleaseContext extends ParserRuleContext {
		public TerminalNode ALPHANUM() { return getToken(IndexNameParser.ALPHANUM, 0); }
		public ReleaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_release; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).enterRelease(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IndexNameListener ) ((IndexNameListener)listener).exitRelease(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof IndexNameVisitor ) return ((IndexNameVisitor<? extends T>)visitor).visitRelease(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReleaseContext release() throws RecognitionException {
		ReleaseContext _localctx = new ReleaseContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_release);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
			match(ALPHANUM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\b#\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\2\2\b\2\4\6\b\n\f\2\3"+
		"\3\2\3\5\2\34\2\16\3\2\2\2\4\30\3\2\2\2\6\32\3\2\2\2\b\34\3\2\2\2\n\36"+
		"\3\2\2\2\f \3\2\2\2\16\17\5\4\3\2\17\20\7\7\2\2\20\21\5\6\4\2\21\22\7"+
		"\7\2\2\22\23\5\b\5\2\23\24\7\7\2\2\24\25\5\n\6\2\25\26\7\7\2\2\26\27\5"+
		"\f\7\2\27\3\3\2\2\2\30\31\7\6\2\2\31\5\3\2\2\2\32\33\t\2\2\2\33\7\3\2"+
		"\2\2\34\35\7\6\2\2\35\t\3\2\2\2\36\37\7\6\2\2\37\13\3\2\2\2 !\7\6\2\2"+
		"!\r\3\2\2\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}