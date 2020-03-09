grammar ICSS;

//--- LEXER: ---
// IF support:
IF: 'if';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
/// Style
stylesheet: stylesheet_body EOF;
stylesheet_body: stylesheet_body_element*;
stylesheet_body_element: variable | style_rule | if_statement;
style_rule: selector OPEN_BRACE style_body CLOSE_BRACE;
style_body: style_body_element*;
style_body_element: style_statement | variable | if_statement; // | style_rule (add to support children)
style_statement: property COLON value SEMICOLON;
selector: LOWER_IDENT | ID_IDENT | CLASS_IDENT;
property: 'width' | 'height' | 'background-color' | 'color';

/// Var
numeric_value: PIXELSIZE | PERCENTAGE | SCALAR;
boolean_value: TRUE | FALSE;
literal: COLOR | boolean_value | expression;
value: literal | variable_identifier;
variable_identifier: CAPITAL_IDENT;
variable: variable_identifier ASSIGNMENT_OPERATOR value SEMICOLON;

/// If
if_statement: IF BOX_BRACKET_OPEN if_expression BOX_BRACKET_CLOSE OPEN_BRACE style_body CLOSE_BRACE;
if_expression: boolean_value | variable_identifier;

/// Math
operator: MUL | PLUS | MIN;
expression: numeric_value | variable_identifier | expression operator expression;
