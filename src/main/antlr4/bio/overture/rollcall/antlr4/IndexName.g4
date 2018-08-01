grammar IndexName;

/*
 * PARSER RULES
 */

indexName
    : entity SEP part SEP shardPrefix SEP shard SEP releasePrefix SEP release
    ;

entity
    : ALPHANUM
    ;

part
    : 'centric'
    | 'text'
    | 'entity'
    ;

shardPrefix
    : ALPHANUM
    ;

shard
    : ALPHANUM
    ;

releasePrefix
    : ALPHANUM
    ;

release
    : ALPHANUM
    ;


/*
 * LEXER RULES
 */

ALPHANUM
    : [a-zA-Z0-9]+
    ;

SEP
    : '_'
    ;

WS
  : [ \t\r\n]+ -> skip
  ;