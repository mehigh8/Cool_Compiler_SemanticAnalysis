parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program: (classes+=class SEMI)+ EOF;

class: CLASS classId=TYPE (INHERITS parentClassId=TYPE)? LBRACE (features+=feature SEMI)* RBRACE;

feature: funcId=ID LPAREN (formals+=formal (COMMA formals+=formal)*)? RPAREN COLON funcType=TYPE LBRACE e=expr RBRACE # funcFeature
       | varId=ID COLON varType=TYPE (ASSIGN e=expr)?                                                                 # varFeature
       ;

formal: formalId=ID COLON formalType=TYPE;

expr: obj=expr (AT parentType=TYPE)? DOT funcId=ID LPAREN (funcParams+=expr (COMMA funcParams+=expr)*)? RPAREN  # explicitDispatch
    | funcId=ID LPAREN (funcParams+=expr (COMMA funcParams+=expr)*)? RPAREN                                     # implicitDispatch
    | IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI                                                 # if
    | WHILE cond=expr LOOP content=expr POOL                                                                    # while
    | LBRACE (exprs+=expr SEMI)+ RBRACE                                                                         # block
    | LET localList+=local (COMMA localList+=local)* IN letContent=expr                                         # let
    | CASE caseExpr=expr OF (caseBranches+=caseBranch)+ ESAC                                                    # case
    | NEW initType=TYPE                                                                                         # new
    | COMPL e=expr                                                                                              # compl
    | ISVOID e=expr                                                                                             # isvoid
    | left=expr op=(MULT | DIV) right=expr                                                                      # multDiv
    | left=expr op=(PLUS | MINUS) right=expr                                                                    # plusMinus
    | left=expr op=(LT | LE | EQUAL) right=expr                                                                 # relational
    | NOT e=expr                                                                                                # not
    | varId=ID ASSIGN e=expr                                                                                    # assign
    | LPAREN e=expr RPAREN                                                                                      # paren
    | varId=ID                                                                                                  # id
    | INTEGER                                                                                                   # integer
    | STRING                                                                                                    # string
    | BOOL                                                                                                      # bool
    ;

local: varId=ID COLON varType=TYPE (ASSIGN varExpr=expr)?;

caseBranch: varId=ID COLON varType=TYPE RESULTS branchExpr=expr SEMI;
