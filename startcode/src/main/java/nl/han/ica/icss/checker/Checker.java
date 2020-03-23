package nl.han.ica.icss.checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.*;

public class Checker {

    // This is a map of all the variables.
    // The global variables will be first, followed by scoped variables.
    // So index 0 variables are global and index 5 variables would be really specificly scoped variables.
    // Scoped variables get removed after an scope is exited.
    private LinkedList<HashMap<String,ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        // Add global scope
        variableTypes.add(new HashMap<String,ExpressionType>());

        checkNode(ast.root);
    }

    private void checkNode(ASTNode node) {
        if (node instanceof Stylerule | node instanceof IfClause) {
            // Add local scope
            variableTypes.add(new HashMap<String, ExpressionType>());
        }

        // Checker logic
        checkNodeForErrors(node);

        // Base case
        ArrayList<ASTNode> nodeChildren = node.getChildren();
        if (nodeChildren.size() == 0) {
            return;
        }

        // Recursion
        for (ASTNode childNode : nodeChildren) {
            checkNode(childNode);
        }

        if (node instanceof Stylerule | node instanceof IfClause) {
            // Remove local scope
            variableTypes.removeLast();
        }
    }

    private void checkNodeForErrors(ASTNode node) {
        // Add new variable assignment to current scope
        if (node instanceof VariableAssignment) {
            VariableAssignment variableAssignment = (VariableAssignment) node;
            variableTypes.getLast().put(variableAssignment.name.name, getExpressionType(variableAssignment.expression));
            return;
        }

        // CH01, Check if variable refrence is defined.
        // CH06, Check if variable refrence is defined in current scope or in global variables.
        if (node instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) node;
            String variableName = variableReference.name;
            Boolean foundVariable = false;
            loop:
            for (HashMap<String,ExpressionType> variables : variableTypes) {
                if (keyIsInMap(variableName, variables)) {
                    foundVariable = true;
                    break loop;
                }
            }
            if (!foundVariable) {
                node.setError("CH01, CH06: Variable " + variableName + " not declared in it's local scope or global scope");
            }
            return;
        }

        // CH04: Check if the value matches the type for an declaration
        if (node instanceof Declaration) {
            Declaration declaration = (Declaration) node;
            ExpressionType expressionType = getExpressionType(declaration.expression);
            if (declaration.property.name.matches("width|height")) {
                if (expressionType == ExpressionType.COLOR) {
                    node.setError("CH04: Value type: Color does not match the property for the declaration. The type shouldn't be COLOR but is COLOR");
                    return;
                }
                return;
            }
            if (declaration.property.name.matches("color|background-color")) {
                if (expressionType != ExpressionType.COLOR) {
                    node.setError("CH04: Value type does not match the property for the declaration. The type should be COLOR and is " + expressionType);
                    return;
                }
                return;
            }
        }

        if (node instanceof Stylerule) {
            Stylerule stylerule = (Stylerule) node;
            ArrayList<Selector> selectors = stylerule.selectors;
            if (selectors.size() == 0) {
                return;
            }
            for (int i = 0; i < selectors.size(); i++) {
                for (int j = i+1; j < selectors.size()-(i+1); j++) {
                    if (selectors.get(i) == selectors.get(j)) {
                        node.setError("EU01: Duplicate selector " + selectors.get(i));
                        return;
                    }
                }
            }
        }

        if (node instanceof Operation) {
            Operation operation = (Operation) node;
            ExpressionType lhs = getExpressionType(operation.lhs);
            ExpressionType rhs = getExpressionType(operation.rhs);
            // CH03: Check if the operation doesn't use colors
            if (lhs == ExpressionType.COLOR || rhs == ExpressionType.COLOR) {
                node.setError("CH03: Colors are not allowed in operations");
                return;
            };
            if (lhs == ExpressionType.BOOL || lhs == ExpressionType.BOOL) {
                node.setError("CH03: Booleans are not allowed in operations");
                return;
            }
            // CH02: Check if the operation uses valid operands.
            validateOperation(operation, operation);
        }

         // CH05: Check if the condition is a bool
         if (node instanceof IfClause) {
            IfClause ifClause = (IfClause) node;
            Expression conditionalExpression = (Expression) ifClause.conditionalExpression;
            ExpressionType conditionalExpressionType = getExpressionType(conditionalExpression);
            // If it is null that means the variable wasn't defined. This check will be done in the variable refrence node
            if (conditionalExpressionType == null) return;
            if (conditionalExpressionType != ExpressionType.BOOL) {
                node.setError("CH05: If statement condition is not of type boolean");
            }
            return;
        }
    }

    private ExpressionType validateOperation(Operation operation, Operation parent) {
        // Get the expression type
        Expression lhs = operation.lhs;
        Expression rhs = operation.rhs;
        ExpressionType lhsType = getExpressionType(lhs);
        ExpressionType rhsType = getExpressionType(rhs);

        // If the left hand side expression type is UNDEFINED it means it's an operation so get the type of it recursively.
        if (lhsType == ExpressionType.UNDEFINED) {
            Operation lhsOperation = (Operation) lhs;
            lhsType = validateOperation(lhsOperation, operation);
            if (lhsType == null) {
                return null;
            }
        }

        // If the right hand side expression type is UNDEFINED it means it's an operation so get the type of it recursively.
        if (rhsType == ExpressionType.UNDEFINED) {
            Operation rhsOperation = (Operation) rhs;
            rhsType = validateOperation(rhsOperation , operation);
            if (rhsType == null) {
                return null;
            }
        }

        // If they are null that means the variables weren't defined. This check will be done in the variable refrence node
        if (lhsType == null || rhsType == null) {
            parent.setError("CH02: Unable to verrify correctness of operation due to invalid variables.");
            return null;
        }

        if (operation instanceof MultiplyOperation) {
            if (lhsType != ExpressionType.SCALAR || rhsType != ExpressionType.SCALAR) {
                // Prefer returning a type that isn't a scalar
                return lhsType != ExpressionType.SCALAR ? lhsType : rhsType;
            }
            parent.setError("CH02: MultiplyOperation should atleast contain one scalar");
            return null;
        }

        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (lhsType == ExpressionType.SCALAR || rhsType == ExpressionType.SCALAR) {
                parent.setError("CH02: Invalid subtract or add operation Scalars are not allowed");
                return null;
            }
            if (lhsType == rhsType) return lhsType;
            parent.setError("CH02: Invalid subtract or add operation " + lhsType + " does not match with " + rhsType);
            return null;
        }

        parent.setError("CH02: Invalid operation " + lhsType + " does not match with " + rhsType);
        return null;
    }

    private ExpressionType getVariableType(String key) {
        ExpressionType type = null;

        // Go from back to front to get the scoped variables first.
        loop:
        for (int i = variableTypes.size() - 1; i >= 0; i--) {
            type = variableTypes.get(i).get(key);
            if (type != null) break loop;
        }

        return type;
    }

    private <T> Boolean keyIsInMap(String key, HashMap<String, T> map) {
        for (String mapKey : map.keySet()) {
            if (mapKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) expression;
            return getVariableType(variableReference.name);
        } else {
            return ExpressionType.UNDEFINED;
        }
    }
}
