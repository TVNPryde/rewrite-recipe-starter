package com.thanh;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ChangeMethodToStatic extends Recipe {

    @Override
    public String getDisplayName() {
        return "Change method to static";
    }

    @Override
    public String getDescription() {
        return "Change method invocations to static method call when using static variable.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration declaration, ExecutionContext p) {
                J.MethodDeclaration md = super.visitMethodDeclaration(declaration, p);

                if (md.hasModifier(J.Modifier.Type.Static)) {
                    return md;
                }

                List<J.VariableDeclarations.NamedVariable> staticVariables =
                        getStaticVariables(getCursor());

                if (staticVariables.stream().anyMatch(v -> FindReferencesToVariable.find(getCursor().getValue(), v).get())) {
                    md = autoFormat(
                            md.withModifiers(
                                    ListUtils.concat(md.getModifiers(),
                                            new J.Modifier(Tree.randomId(), Space.EMPTY, Markers.EMPTY, J.Modifier.Type.Static, Collections.emptyList()))
                            ), p);
                }
                return md;
            }
        };
    }

    private List<J.VariableDeclarations.NamedVariable> getStaticVariables(Cursor cursor) {
        return ((J.Block) cursor.getParentTreeCursor().getValue()).getStatements().stream()
                .filter(s -> s instanceof J.VariableDeclarations &&
                        ((J.VariableDeclarations) s).hasModifier(J.Modifier.Type.Static))
                .map(s -> ((J.VariableDeclarations) s).getVariables().get(0))
                .collect(Collectors.toList());
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    private static class FindReferencesToVariable extends JavaIsoVisitor<AtomicBoolean> {

        J.VariableDeclarations.NamedVariable variable;

        static AtomicBoolean find(J j, J.VariableDeclarations.NamedVariable variable) {
            return new FindReferencesToVariable(variable).reduce(j, new AtomicBoolean());
        }

        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, AtomicBoolean hasIdentifier) {
            if (identifier.getSimpleName().equals(variable.getSimpleName()) &&
                    (identifier.getFieldType()) instanceof JavaType.Variable) {
                hasIdentifier.set(true);
            }

            return identifier;
        }
    }
}
