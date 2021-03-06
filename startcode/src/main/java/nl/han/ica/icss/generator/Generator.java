package nl.han.ica.icss.generator;

import java.util.ArrayList;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

public class Generator {
	private int scopeLevel = 0;

	public String generate(AST ast) {
		StringBuilder builder = new StringBuilder();
		generateNode(ast.root, builder);
        return builder.toString();
	}

	public void generateNode(ASTNode node, StringBuilder builder) {
		enterGenerateNode(node, builder);
		// Base case
		ArrayList<ASTNode> nodeChildren = node.getChildren();
		if (nodeChildren.size() == 0) {
			exitGenerateNode(node, builder);
			return;
		}

		// Recursion
		loop:
		for (ASTNode childNode : nodeChildren) {
			// Skip VariableAssignments
			if (childNode instanceof VariableAssignment) {
				continue loop;
			}
			generateNode(childNode, builder);
		}

		exitGenerateNode(node, builder);
	}

	private void enterGenerateNode(ASTNode node, StringBuilder builder) {
		if (node instanceof Stylerule) {
			Stylerule stylerule = (Stylerule) node;
			ArrayList<Selector> selectors = stylerule.selectors;
			ArrayList<Selector> parentSelectors = stylerule.parentSelectors;
			if (parentSelectors.size() > 0) buildParentSelectors(builder, parentSelectors);
			buildSelectors(builder, selectors);
			builder.append(" {\n");
			scopeLevel++;
		}

		if (node instanceof Declaration) {
			addTabs(builder);
		}

		if (node instanceof PropertyName) {
			PropertyName property = (PropertyName) node;
			builder.append(property.name)
					.append(": ");
		}

		if (node instanceof PixelLiteral) {
			PixelLiteral literal = (PixelLiteral) node;
			builder.append(literal.value).append("px");
		}

		if (node instanceof PercentageLiteral) {
			PercentageLiteral literal = (PercentageLiteral) node;
			builder.append(literal.value);
		}

		if (node instanceof ColorLiteral) {
			ColorLiteral literal = (ColorLiteral) node;
			builder.append(literal.value);
		}
	}

	private void exitGenerateNode(ASTNode node, StringBuilder builder) {
		if (node instanceof Stylerule) {
			scopeLevel--;
			builder.append("}\n\n");
		}

		if (node instanceof Declaration) {
			builder.append("\n");
		}

		if (node instanceof Literal) {
			builder.append(";");
		}
	}

	private void buildSelectors(StringBuilder builder, ArrayList<Selector> selectors) {
		builder.append(selectors.get(0).toString());
		for (int i = 1; i < selectors.size(); i++) {
			builder.append(", ")
				.append(selectors.get(i));
		}
	}

	private void buildParentSelectors(StringBuilder builder, ArrayList<Selector> parentSelectors) {
		builder.append(parentSelectors.get(0).toString());
		for (int i = 1; i < parentSelectors.size(); i++) {
			builder.append(" ")
				.append(parentSelectors.get(i));
		}
		builder.append(" ");
	}

	private void addTabs(StringBuilder builder) {
		for (int i = 0; i < scopeLevel; i++) {
			builder.append("\t");
		}
	}
}
