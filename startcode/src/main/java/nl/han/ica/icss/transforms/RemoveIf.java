package nl.han.ica.icss.transforms;

import java.util.ArrayList;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

public class RemoveIf implements Transform {

    @Override
    public void apply(AST ast) {
        removeIf(ast.root);
    }

    public void removeIf(ASTNode node) {
        if (node instanceof IfClause) {
            IfClause ifClause = (IfClause) node;
            ArrayList<ASTNode> body = ifClause.body;
            checkBodyForIfStatements(body, node);
        }

        if (node instanceof Stylerule) {
            Stylerule stylerule = (Stylerule) node;
            ArrayList<ASTNode> body = stylerule.body;
            checkBodyForIfStatements(body, node);
        }

        // Base case
        ArrayList<ASTNode> nodeChildren = node.getChildren();
		if (nodeChildren.size() == 0) {
			return;
        }

        // Recursion
		for (ASTNode childNode : nodeChildren) {
			removeIf(childNode);
		}
    }

    private void checkBodyForIfStatements(ArrayList<ASTNode> body, ASTNode previous) {
        ArrayList<ASTNode> addList = new ArrayList<ASTNode>();
        ArrayList<ASTNode> removeList = new ArrayList<ASTNode>();
        for (ASTNode bodyNode : body) {
            if (bodyNode instanceof IfClause) {
                IfClause ifClause = (IfClause) bodyNode;
                BoolLiteral condition = (BoolLiteral) ifClause.conditionalExpression;
                if (condition.value == true) {
                    removeIf(bodyNode);
                    addList.addAll(ifClause.body);
                    // Add ifClause to an array that deletes them later to avoid ConcurrentModificationException
                    removeList.add(bodyNode);
                } else {
                    // Add ifClause to an array that deletes them later to avoid ConcurrentModificationException
                    removeList.add(bodyNode);
                }
            }
        }
        for (ASTNode bodyNode : addList) {
            previous.addChild(bodyNode);
        }
        for (ASTNode ifClause : removeList) {
            body.remove(ifClause);
        }
    }
}
