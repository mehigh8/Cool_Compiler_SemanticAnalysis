lexer grammar CoolLexer;

tokens { ERROR } 

@header{
    package cool.lexer;	
}

@members{    
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }

    private void checkString(String str) {
        char[] initChars = str.toCharArray();
        String finalString = "";
        for (int i = 1; i < initChars.length - 1; i++) {
            if (initChars[i] == '\\') {
                if (i + 1 < initChars.length) {
                    switch (initChars[i + 1]) {
                        case 'n': finalString += "\n";
                                  break;
                        case 't': finalString += "\t";
                                  break;
                        case 'b': finalString += "\b";
                                  break;
                        case 'f': finalString += "\f";
                                  break;
                        default: finalString += initChars[i + 1];
                    }
                    i++;
                }
            } else {
                finalString += initChars[i];
            }
        }
        if (finalString.length() > 1024)
            raiseError("String constant too long");
        else
            setText(finalString);
    }
}

WS: [ \n\f\r\t]+ -> skip;

// Keywords
IF: [iI][fF];
THEN: [tT][hH][eE][nN];
ELSE: [eE][lL][sS][eE];
FI: [fF][iI];
CLASS: [cC][lL][aA][sS][sS];
INHERITS: [iI][nN][hH][eE][rR][iI][tT][sS];
WHILE: [wW][hH][iI][lL][eE];
LOOP: [lL][oO][oO][pP];
POOL: [pP][oO][oO][lL];
CASE: [cC][aA][sS][eE];
OF: [oO][fF];
ESAC: [eE][sS][aA][cC];
LET: [lL][eE][tT];
IN: [iI][nN];
NEW: [nN][eE][wW];
NOT: [nN][oO][tT];
BOOL: ('t'[rR][uU][eE]) | ('f'[aA][lL][sS][eE]);
ISVOID: [iI][sS][vV][oO][iI][dD];

// Integers and identifiers
fragment LETTER: [a-zA-Z];
fragment DIGIT: [0-9];
TYPE: [A-Z](LETTER | DIGIT | '_')*;
ID: [a-z](LETTER | DIGIT | '_')*;
INTEGER: DIGIT+;

// String
fragment NEWLINE: '\r'? '\n';
STRING: '"'('\\"' | ('\\' NEWLINE) | ~'\u0000')*? ( '"' { checkString(getText()); }
                                          | NEWLINE { raiseError("Unterminated string constant"); }
                                          | EOF { raiseError("EOF in string constant"); });
STRING_NULL: '"'('\u0000' | '\\"' | ('\\' NEWLINE) | .)*? ( '"' { raiseError("String contains null character"); }
                                                  | NEWLINE { raiseError("Unterminated string constant"); }
                                                  | EOF { raiseError("EOF in string constant"); });

// Other symbols
COLON: ':';
SEMI : ';';
COMMA : ',';
ASSIGN : '<-';
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
PLUS : '+';
MINUS : '-';
MULT : '*';
DIV : '/';
EQUAL : '=';
LT : '<';
LE : '<=';
RESULTS: '=>';
DOT: '.';
AT: '@';
COMPL: '~';

// Comments
LINE_COMMENT: '--' .*? (NEWLINE | EOF) -> skip;
BLOCK_COMMENT: '(*' (BLOCK_COMMENT | .)*? ('*)' { skip(); } | EOF { raiseError("EOF in comment"); });

// Other errors
END_OF_COMMENT: '*)' { raiseError("Unmatched *)"); };
INVALID_CHAR: . { raiseError("Invalid character: " + getText()); };