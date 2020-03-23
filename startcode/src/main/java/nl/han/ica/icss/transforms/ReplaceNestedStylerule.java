package nl.han.ica.icss.transforms;

import java.util.ArrayList;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Selector;
import nl.han.ica.icss.ast.Stylerule;

public class ReplaceNestedStylerule implements Transform {
    @Override
    public void apply(AST ast) {
        for (ASTNode bodyNode : ast.root.body) {
            if (bodyNode instanceof Stylerule) {
                replaceNestedStylerules((Stylerule) bodyNode, ast.root);
            }
        }
    }

    public void replaceNestedStylerules(Stylerule stylerule, ASTNode parent) {
        for (ASTNode bodyNode : stylerule.body) {
            if (bodyNode instanceof Stylerule) {
                replaceNestedStylerules((Stylerule) bodyNode, stylerule);
                replaceNestedStylerule((Stylerule) bodyNode, stylerule, parent);
            }
        }
    }

    private void replaceNestedStylerule(Stylerule stylerule, Stylerule parent, ASTNode grandParent) {
        ArrayList<Selector> parents = parent.selectors;
        if (parents.size() == 1) {
            stylerule.addParentSelector(parents.get(0));
            grandParent.addChild(stylerule);
        } else {
            for (Selector parentSelector : parents) {
                Stylerule styleruleCopy = stylerule.copy();
                styleruleCopy.addParentSelector(parentSelector);
                grandParent.addChild(styleruleCopy);
            }
        }
        parent.removeChild(stylerule);
    }
}
