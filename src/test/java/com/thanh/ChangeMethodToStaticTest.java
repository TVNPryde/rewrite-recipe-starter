package com.thanh;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class ChangeMethodToStaticTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangeMethodToStatic())
            .parser(JavaParser.fromJavaVersion()
                .logCompilationWarningsAndErrors(true));
    }

    @Test
    void methodTargetToStatic() {
        rewriteRun(
            spec -> spec
                .parser(JavaParser.fromJavaVersion()
                    .logCompilationWarningsAndErrors(false)),
            java("""
                    class A {
                        private static String word = "word";
                        
                        public String getWord() {
                            return word;
                        } 
                        
                        public void setWord(String newWord) {
                            word = newWord;
                        }
                    }
                """,
                """
                    class A {
                        private static String word = "word";
                        
                        public static String getWord() {
                            return word;
                        }
        
                        public static void setWord(String newWord) {
                            word = newWord;
                        }
                    }
            """)
        );
    }

    @Test
    void methodTargetToStaticPartially() {
        rewriteRun(
            spec -> spec
                .parser(JavaParser.fromJavaVersion()
                    .logCompilationWarningsAndErrors(false)),
            java("""
                    class A {
                        private static String word = "word";
                        
                        public static String getWord() {
                            return word;
                        } 
                        
                        public void setWord(String newWord) {
                            word = newWord;
                        }
                    }
                """,
                """
                    class A {
                        private static String word = "word";
                        
                        public static String getWord() {
                            return word;
                        }
        
                        public static void setWord(String newWord) {
                            word = newWord;
                        }
                    }
            """)
        );
    }

    @Test
    void methodTargetStayTheSame() {
        rewriteRun(
            spec -> spec
                .parser(JavaParser.fromJavaVersion()
                    .logCompilationWarningsAndErrors(false)),
            java("""
                class A {
                    private String word = "word";
    
                    public String getWord() {
                        return word;
                    }
    
                    public void setWord(String newWord) {
                        word = newWord;
                    }
                }
            """)
        );
    }

    /*
     * The two test below were disabled due to what Sam considers a bug with
     * {@link J.Identifier}. The parameter and the local variables with the
     * same name {@literal word} and type as {@link JavaType.Variable} static
     * variable in comparison. The {@link Visitor} is currently considers
     * those variables as the same as the static variable.
     */

    @Disabled
    @Test
    void methodTargetToStatic_OverriddenLocal() {
        rewriteRun(
            spec -> spec
                .parser(JavaParser.fromJavaVersion()
                    .logCompilationWarningsAndErrors(false)),
            java("""
                    class A {
                        private static String word = "word";
                        
                        public void setMethod(String newWord) {
                            String word = newWord;
                            System.out.println(word);
                        }
                    }
                """,
                """
                    class A {
                        private static String word = "word";
                        
                        public void setMethod(String newWord) {
                            String word = newWord;
                            System.out.println(word);
                        }
                    }
            """)
        );
    }

    @Disabled
    @Test
    void methodTargetToStatic_OverriddenParameter() {
        rewriteRun(
            spec -> spec
                .parser(JavaParser.fromJavaVersion()
                    .logCompilationWarningsAndErrors(false)),
            java("""
                    class A {
                        private static String word = "word";
                        
                        public void setMethod(String word) {
                            // ignored
                        }
                    }
                """,
                """
                    class A {
                        private static String word = "word";
                        
                        public void setMethod(String word) {
                            // ignored
                        }
                    }
            """)
        );
    }
}
