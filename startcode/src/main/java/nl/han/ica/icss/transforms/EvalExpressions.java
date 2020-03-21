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

    private void evalExpressions(ASTNode node, ASTNode previous) {
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

    private void enterEvaluation(ASTNode target, ASTNode previous) {
        if (target instanceof Expression) {
            Expression expression = (Expression) target;
            evalExpression(expression, previous);
        }

        if (target instanceof Stylerule || target instanceof IfClause) {
            // Add local scope
            variableValues.add(new HashMap<String,Literal>());
        }

        // Add new variable assignment to current scope
        if (target instanceof VariableAssignment) {
            VariableAssignment variableAssignment = (VariableAssignment) target;
            variableValues.getLast().put(variableAssignment.name.name, getLiteralFromExpression(variableAssignment.expression));
        }
    }

    private void exitEvaluation(ASTNode target, ASTNode previous) {
        if (target instanceof Stylerule || target instanceof IfClause) {
            // Remove local scope
            variableValues.removeLast();
        }
    }

    private void evalExpression(Expression expression, ASTNode previous) {
        if (expression instanceof VariableReference) {
            VariableReference variableRefrence = (VariableReference) expression;
            replaceVariableWithLiteral(variableRefrence, previous);
        }

        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            replaceOperationWithLiteral(operation, previous);
        }
    }

    private void replaceVariableWithLiteral(VariableReference variableReference, ASTNode previous) {
        Literal literal = getVariableLiteral(variableReference.name);
        previous.removeChild(variableReference);
        previous.addChild(literal);
    }

    private void replaceOperationWithLiteral(Operation operation, ASTNode previous) {
        Literal literal = transformOperationToLiteral(operation);
        previous.removeChild(operation);
        previous.addChild(literal);
    }

    private Literal getLiteralFromExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        }

        if (expression instanceof VariableReference) {
            VariableReference variableRefrence = (VariableReference) expression;
            return getVariableLiteral(variableRefrence.name);
        }

        Operation operation = (Operation) expression;
        return transformOperationToLiteral(operation);
    }

    private Literal transformOperationToLiteral(Operation operation) {
        if (operation instanceof AddOperation) {

        }
        return new PixelLiteral(10); // TODO this is temp
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
