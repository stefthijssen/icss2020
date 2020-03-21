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
        // Add global variables to scope
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
                // TODO: Optional deconstruct this error into two seperate errors.
                node.setError("CH01, CH06: Variable " + variableName + " not declared in it's local scope or global scope");
            }
            return;
        }

        // CH04: Check if the value matches the type for an declaration
        if (node instanceof Declaration) {
            Declaration declaration = (Declaration) node;
            if (declaration.property.name.matches("width|height")) {
                if (declaration.expression instanceof VariableReference) {
                    VariableReference variableRefrence = (VariableReference) declaration.expression;
                    ExpressionType type = getVariableType(variableRefrence.name);
                    // This check is unneeded but nice to have for the flow of the code
                    if (type == null) {
                        // This error wil be caught in the VariableRefrence node
                        return;
                    }
                    if (type == ExpressionType.COLOR) {
                        node.setError("CH04: Value type: Color does not match the property for the declaration");
                    }
                    return;
                }
                if (declaration.expression instanceof ColorLiteral) {
                    node.setError("CH04: Value type: Color does not match the property for the declaration");
                    return;
                }
            }
            if (declaration.property.name.matches("color|background-color")) {
                if (declaration.expression instanceof VariableReference) {
                    VariableReference variableRefrence = (VariableReference) declaration.expression;
                    ExpressionType type = getVariableType(variableRefrence.name);
                    if (type == null) {
                        // This error wil be caught later in the VariableRefrence node
                        return;
                    }
                    if (type != ExpressionType.COLOR) {
                        node.setError("CH04: Value type does not match the property for the declaration");
                    }
                    return;
                }
                if (!(declaration.expression instanceof ColorLiteral)) {
                    node.setError("CH04: Value type does not match the property for the declaration");
                    return;
                }
            }
        }

        // CH02: Check if plus operations are done with the same type
        if (node instanceof AddOperation) {
            AddOperation addOperation = (AddOperation) node;
            Expression lhs = addOperation.lhs;
            Expression rhs = addOperation.rhs;
            ExpressionType lhsType = getExpressionType(lhs);
            ExpressionType rhsType = getExpressionType(rhs);
            // If they are null that means the variables weren't defined. This check will be done in the variable refrence node
            if (lhsType == null || rhsType == null) return;
            if (lhsType == rhsType || lhsType == ExpressionType.UNDEFINED || rhsType == ExpressionType.UNDEFINED) return;
            node.setError("CH02: The types of the left hand side and the right hand side of the operator are not equal");
            return;
        }

        // CH02: Check if subtract operations are done with the same type
        if (node instanceof SubtractOperation) {
            SubtractOperation subtractOperation = (SubtractOperation) node;
            Expression lhs = subtractOperation.lhs;
            Expression rhs = subtractOperation.rhs;
            ExpressionType lhsType = getExpressionType(lhs);
            ExpressionType rhsType = getExpressionType(rhs);
            // If they are null that means the variables weren't defined. This check will be done in the variable refrence node
            if (lhsType == null || rhsType == null) return;
            // Because i don't know how to give errors for UNDEFINED i just allow them.
            if (lhsType == rhsType || lhsType == ExpressionType.UNDEFINED || rhsType == ExpressionType.UNDEFINED) return;
            node.setError("CH02: The types of the left hand side and the right hand side of the operator are not equal");
        }

        // CH02: Check if multiply operations contain atleast one scalar
        if (node instanceof MultiplyOperation) {
            MultiplyOperation multiplyOperation = (MultiplyOperation) node;
            Expression lhs = multiplyOperation.lhs;
            Expression rhs = multiplyOperation.rhs;
            ExpressionType lhsType = getExpressionType(lhs);
            ExpressionType rhsType = getExpressionType(rhs);
            // If they are null that means the variables weren't defined. This check will be done in the variable refrence node
            if (lhsType == null || rhsType == null) return;
            if (lhsType != ExpressionType.SCALAR || rhsType != ExpressionType.SCALAR
                // Because i don't know how to give errors for UNDEFINED i just allow them.
                || lhsType == ExpressionType.UNDEFINED || rhsType == ExpressionType.UNDEFINED) {
                return;
            }
            node.setError("CH02: Multiply operation contains no scalars");
        }

        // CH03: Check if the operation doesn't use colors
        if (node instanceof Operation) {
            Operation operation = (Operation) node;
            if (operation.lhs instanceof ColorLiteral && operation.rhs instanceof ColorLiteral) {
                node.setError("CH03: Left hand side and right hand side of operation is a color");
                return;
            }
            if (operation.lhs instanceof ColorLiteral) {
                node.setError("CH03: Left hand side of operation is a color");
                return;
            }
            if (operation.rhs instanceof ColorLiteral) {
                node.setError("CH03: Right hand side of operation is a color");
                return;
            }
        }

         // CH05:
         if (node instanceof IfClause) {
            IfClause ifClause = (IfClause) node;
            Expression conditionalExpression = (Expression) ifClause.conditionalExpression;
            ExpressionType conditionalExpressionType = getExpressionType(conditionalExpression);
            // If it is null that means the variable wasn't defined. This check will be done in the variable refrence node
            if (conditionalExpressionType== null) return;
            if (conditionalExpressionType != ExpressionType.BOOL) {
                node.setError("CH05: If statement condition is not of type boolean");
            }
            return;
        }
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
