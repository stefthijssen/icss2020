package nl.han.ica.icss.transforms;

import java.util.ArrayList;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Selector;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.Stylesheet;

public class ReplaceNestedStylerule implements Transform {
    ArrayList<ASTNode> addList = new ArrayList<ASTNode>();
    ArrayList<ASTNode> removeList = new ArrayList<ASTNode>();

    @Override
    public void apply(AST ast) {
        iterateOverStylesheet(ast.root);
        if (addList.size() > 0) {
            processAddList(ast.root);
            iterateOverStylesheet(ast.root);
        }
    }

    private void iterateOverStylesheet(Stylesheet stylesheet) {
        for (ASTNode bodyNode : stylesheet.body) {
            if (bodyNode instanceof Stylerule) {
                replaceNestedStylerules((Stylerule) bodyNode, stylesheet);
            }
        }
    }

    public void replaceNestedStylerules(Stylerule stylerule, ASTNode parent) {
        for (ASTNode bodyNode : stylerule.body) {
            if (bodyNode instanceof Stylerule) {
                replaceNestedStylerule((Stylerule) bodyNode, stylerule, parent);
                replaceNestedStylerules((Stylerule) bodyNode, stylerule);
            }
        }
        processRemoveList(stylerule);
    }

    private void processAddList(ASTNode target) {
        for (ASTNode node : addList) {
            target.addChild(node);
        }
    }

    private void processRemoveList(ASTNode target) {
        for (ASTNode node : removeList) {
            target.removeChild(node);
        }
    }

    private void replaceNestedStylerule(Stylerule stylerule, Stylerule parent, ASTNode grandParent) {
        ArrayList<Selector> selectorsOfParent = parent.selectors;
        ArrayList<Selector> parentSelectorsOfParent = parent.parentSelectors;
        if (selectorsOfParent.size() == 1) {
            // Copy the parent selectors from the parent.
            for (Selector selector : parentSelectorsOfParent) {
                stylerule.addParentSelector(selector);
            }
            // Add the selector of the parent as a parent selector
            stylerule.addParentSelector(selectorsOfParent.get(0));
            addList.add(stylerule);
        }
        removeList.add(stylerule);
    }
}
