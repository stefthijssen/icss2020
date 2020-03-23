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
stylesheet: stylesheet_element+ EOF;
stylesheet_element: variable | style_rule;
style_rule: selector OPEN_BRACE style_rule_element+ CLOSE_BRACE;
style_rule_element: variable | declaration | if_statement;
declaration: property COLON value SEMICOLON;
selector: selector_element | selector_element ',' selector;
selector_element: LOWER_IDENT | ID_IDENT | CLASS_IDENT;
property: 'width' | 'height' | 'background-color' | 'color';

/// Math
expression: numeric_value | variable_identifier | expression MUL expression | expression (PLUS | MIN) expression;

/// Var
variable: variable_identifier ASSIGNMENT_OPERATOR value SEMICOLON;
variable_identifier: CAPITAL_IDENT;
value: literal | variable_identifier | expression;
numeric_value: PIXELSIZE | PERCENTAGE | SCALAR;
boolean_value: TRUE | FALSE;
literal: COLOR | boolean_value;

/// If
if_statement: IF BOX_BRACKET_OPEN if_expression BOX_BRACKET_CLOSE OPEN_BRACE style_rule_element+ CLOSE_BRACE;
if_expression: boolean_value | variable_identifier;
