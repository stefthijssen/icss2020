package nl.han.ica.icss.parser;

import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.*;
import nl.han.ica.icss.ast.types.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.parser.ICSSParser.Boolean_valueContext;
import nl.han.ica.icss.parser.ICSSParser.ExpressionContext;
import nl.han.ica.icss.parser.ICSSParser.If_statementContext;
import nl.han.ica.icss.parser.ICSSParser.LiteralContext;
import nl.han.ica.icss.parser.ICSSParser.Numeric_valueContext;
import nl.han.ica.icss.parser.ICSSParser.OperatorContext;
import nl.han.ica.icss.parser.ICSSParser.VariableContext;
import nl.han.ica.icss.parser.ICSSParser.Variable_identifierContext;
/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private Stack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new Stack<>();
	}

	// Create stylesheet and push to stack
	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(new Stylesheet());
	}

	// Create variable, connect variable to stylesheet or stylerule and push variable to stack
	@Override
	public void enterVariable(VariableContext ctx) {
		VariableAssignment variableAssignment = new VariableAssignment();
		currentContainer.peek().addChild(variableAssignment);
		currentContainer.push(variableAssignment);
	}

	@Override
	public void enterVariable_identifier(Variable_identifierContext ctx) {
		currentContainer.peek().addChild(new VariableReference(ctx.getText()));
	}

	// Create if clause, connect if clause to stylesheet or stylerule and push if clause to stack
	@Override
	public void enterIf_statement(If_statementContext ctx) {
		IfClause ifClause = new IfClause();
		currentContainer.peek().addChild(ifClause);
		currentContainer.push(ifClause);
	}

	// Create stylerule, connect stylerule to stylesheet and push stylerule to stack
	@Override
	public void enterStyle_rule(ICSSParser.Style_ruleContext ctx) {
		Stylerule stylerule = new Stylerule();
		currentContainer.peek().addChild(stylerule);
		currentContainer.push(stylerule);
	}

	// Create Selector and connect to stylerule.
	@Override
	public void enterSelector(ICSSParser.SelectorContext ctx) {
		TerminalNode lower_ident = ctx.LOWER_IDENT();
		if (lower_ident != null) {
			currentContainer.peek().addChild(new TagSelector(ctx.children.get(0).getText()));
			return;
		}
		TerminalNode id_ident = ctx.ID_IDENT();
		if (id_ident != null) {
			currentContainer.peek().addChild(new IdSelector(ctx.children.get(0).getText()));
			return;
		}
		TerminalNode class_ident = ctx.CLASS_IDENT();
		if (class_ident != null) {
			currentContainer.peek().addChild(new ClassSelector(ctx.children.get(0).getText()));
			return;
		}
	}

	// Create declaration, connect declaration to stylerule and push declaration to stack
	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();
		currentContainer.peek().addChild(declaration);
		currentContainer.push(declaration);
	}

	// Create expression, connect expression to expression or value add expression to stack
	@Override
	public void enterExpression(ExpressionContext ctx) {
		List<ExpressionContext> expressionElements = ctx.expression();
		if (expressionElements.size() == 3) {
			OperatorContext operator = expressionElements.get(1).operator();
			TerminalNode min = operator.MIN();
			if (min != null) {
				SubtractOperation operation = new SubtractOperation();
				operation.addChild(new ScalarLiteral(expressionElements.get(0).getText()));
				operation.addChild(new ScalarLiteral(expressionElements.get(2).getText()));
				currentContainer.peek().addChild(operation);
				return;
			}
			TerminalNode plus = operator.PLUS();
			if (plus != null) {
				AddOperation operation = new AddOperation();
				operation.addChild(new ScalarLiteral(expressionElements.get(0).getText()));
				operation.addChild(new ScalarLiteral(expressionElements.get(2).getText()));
				currentContainer.peek().addChild(operation);
				return;
			}
			TerminalNode mul = operator.MUL();
			if (mul != null) {
				MultiplyOperation operation = new MultiplyOperation();
				operation.addChild(new ScalarLiteral(expressionElements.get(0).getText()));
				operation.addChild(new ScalarLiteral(expressionElements.get(2).getText()));
				currentContainer.peek().addChild(operation);
				return;
			}
		}
	}

	// Create property and connect property to declaration
	@Override
	public void enterProperty(ICSSParser.PropertyContext ctx) {
		currentContainer.peek().addChild(new PropertyName(ctx.children.get(0).getText()));
	}

	// Create and connect boolean value to literal or if expression
	@Override
	public void enterBoolean_value(Boolean_valueContext ctx) {
		currentContainer.peek().addChild(new BoolLiteral(ctx.getText()));
	}

	// Create and connect literal to value if literal is an color
	@Override
	public void enterLiteral(LiteralContext ctx) {
		TerminalNode color = ctx.COLOR();
		if (color != null) {
			currentContainer.peek().addChild(new ColorLiteral(color.getText()));
		}
	}

	// Create and connect numeric value to expression
	@Override
	public void enterNumeric_value(Numeric_valueContext ctx) {
		TerminalNode pixelsize = ctx.PIXELSIZE();
		if (pixelsize != null) {
			currentContainer.peek().addChild(new PixelLiteral(pixelsize.getText()));
			return;
		}
		TerminalNode percentage = ctx.PERCENTAGE();
		if (percentage != null) {
			currentContainer.peek().addChild(new PercentageLiteral(percentage.getText()));
			return;
		}
		TerminalNode scalar = ctx.SCALAR();
		if (scalar != null) {
			currentContainer.peek().addChild(new ScalarLiteral(scalar.getText()));
			return;
		}
	}

	// Remove if clause from stack
	@Override
	public void exitIf_statement(If_statementContext ctx) {
		currentContainer.pop();
	}

	// Remove variable from stack
	@Override
	public void exitVariable(VariableContext ctx) {
		currentContainer.pop();
	}

	// Remove declaration from stack
	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.pop();
	}

	// Remove stylerule from stack
	@Override
	public void exitStyle_rule(ICSSParser.Style_ruleContext ctx) {
		currentContainer.pop();
	}

	// Remove stylesheet from stack and replace current ast root with it
	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		ast.root = (Stylesheet) currentContainer.pop();
	}

    public AST getAST() {
        return ast;
    }
}
