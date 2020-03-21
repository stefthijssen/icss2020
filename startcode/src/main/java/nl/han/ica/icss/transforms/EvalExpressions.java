package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class EvalExpressions implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public EvalExpressions() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new LinkedList<>();
        // Add global scope
        variableValues.add(new HashMap<String,Literal>());
        evalExpressions(ast.root, null);
    }

    private void evalExpressions(ASTNode node, ASTNode parent) {
        // Base case
        ArrayList<ASTNode> nodeChildren = node.getChildren();
		if (nodeChildren.size() == 0) {
			return;
        }

        // Recursion
		for (ASTNode childNode : nodeChildren) {
            enterEvaluation(childNode, node);

            evalExpressions(childNode, node);

            exitEvaluation(childNode, node);
		}
    }

    private void enterEvaluation(ASTNode node, ASTNode parent) {
        if (node instanceof Expression) {
            Expression expression = (Expression) node;
            evalExpression(expression, parent);
        }

        if (node instanceof Stylerule || node instanceof IfClause) {
            // Add local scope
            variableValues.add(new HashMap<String,Literal>());
        }

        // Add new variable assignment to current scope
        if (node instanceof VariableAssignment) {
            VariableAssignment variableAssignment = (VariableAssignment) node;
            variableValues.getLast().put(variableAssignment.name.name, getLiteralFromExpression(variableAssignment.expression));
        }
    }

    private void exitEvaluation(ASTNode node, ASTNode parent) {
        if (node instanceof Stylerule || node instanceof IfClause) {
            // Remove local scope
            variableValues.removeLast();
        }
    }

    private void evalExpression(Expression expression, ASTNode parent) {
        if (expression instanceof VariableReference) {
            VariableReference variableRefrence = (VariableReference) expression;
            replaceVariableWithLiteral(variableRefrence, parent);
        }

        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            replaceOperationWithLiteral(operation, parent);
        }
    }

    private void replaceVariableWithLiteral(VariableReference variableReference, ASTNode parent) {
        Literal literal = getVariableLiteral(variableReference.name);
        parent.removeChild(variableReference);
        parent.addChild(literal);
    }

    private void replaceOperationWithLiteral(Operation operation, ASTNode parent) {
        Literal literal = getLiteralFromOperation(operation);
        parent.removeChild(operation);
        parent.addChild(literal);
    }

    private Literal getLiteralFromExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        }

        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            return getLiteralFromOperation(operation);
        }

        VariableReference variableRefrence = (VariableReference) expression;
        return getVariableLiteral(variableRefrence.name);
    }

    private Literal getLiteralFromOperation(Operation operation) {
        Literal lhsLiteral = getLiteralFromExpression(operation.lhs);
        Literal rhsLiteral = getLiteralFromExpression(operation.rhs);
        int operationValue = calculateOperation(operation, lhsLiteral, rhsLiteral);

        if (lhsLiteral instanceof PixelLiteral || rhsLiteral instanceof PixelLiteral) {
            return new PixelLiteral(operationValue);
        }
        if (lhsLiteral instanceof PercentageLiteral || rhsLiteral instanceof PercentageLiteral) {
            return new PercentageLiteral(operationValue);
        }
        return new ScalarLiteral(operationValue);
    }

    private int calculateOperation(Operation operation, Literal lhsLiteral, Literal rhsLiteral) {
        // Add
        if (operation instanceof AddOperation) {
            return getValueFromLiteral(lhsLiteral) + getValueFromLiteral(rhsLiteral);
        }

        // Subtract
        if (operation instanceof SubtractOperation) {
            return getValueFromLiteral(lhsLiteral) - getValueFromLiteral(rhsLiteral);
        }

        // Multiply
        return getValueFromLiteral(lhsLiteral) * getValueFromLiteral(rhsLiteral);
    }

    private int getValueFromLiteral(Literal literal) {
        if (literal instanceof PixelLiteral) {
            PixelLiteral pixelLiteral = (PixelLiteral) literal;
            return pixelLiteral.value;
        }
        if (literal instanceof PercentageLiteral) {
            PercentageLiteral percentageLiteral = (PercentageLiteral) literal;
            return percentageLiteral.value;
        }
        if (literal instanceof ScalarLiteral) {
            ScalarLiteral scalarLiteral = (ScalarLiteral) literal;
            return scalarLiteral.value;
        }
        return 0;
    }

    private Literal getVariableLiteral(String key) {
        Literal literal = null;

        // Go from back to front to get the scoped variables first.
        loop:
        for (int i = variableValues.size() - 1; i >= 0; i--) {
            literal = variableValues.get(i).get(key);
            if (literal != null) break loop;
        }

        return literal;
    }
}
