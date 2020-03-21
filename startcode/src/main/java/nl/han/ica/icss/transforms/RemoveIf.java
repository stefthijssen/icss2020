package nl.han.ica.icss.transforms;

import java.util.ArrayList;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

public class RemoveIf implements Transform {

    @Override
    public void apply(AST ast) {


    }

    public void removeIf(ASTNode node) {
        if (node instanceof IfClause) {
            IfClause stylerule = (IfClause) node;
            ArrayList<ASTNode> body = stylerule.body;
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
        for (ASTNode bodyNode : body) {
            if (bodyNode instanceof IfClause) {
                IfClause ifClause = (IfClause) bodyNode;
                BoolLiteral condition = (BoolLiteral) ifClause.conditionalExpression;
                if (condition.value) {
                    ArrayList<ASTNode> ifClauseBody = ifClause.body;
                    for (ASTNode ifClauseBodyNode : ifClauseBody) {
                        previous.addChild(ifClauseBodyNode);
                    }
                    previous.removeChild(ifClause);
                } else {
                    previous.removeChild(ifClause);
                }
            }
        }
    }
}
