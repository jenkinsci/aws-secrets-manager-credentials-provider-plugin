package io.jenkins.plugins.credentials.secretsmanager.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DeferredEnvironmentVariables implements TestRule {

    private final Map<String, Supplier<String>> buffer = new HashMap<>();

    /**
     * Specify an env var whose value is only available later on (e.g. after another JUnit test resource has finished
     * setting up).
     */
    public DeferredEnvironmentVariables set(String name, Supplier<String> value) {
        buffer.put(name, value);
        return this;
    }

    /**
     * Specify an env var whose value is immediately available.
     */
    public DeferredEnvironmentVariables set(String name, String value) {
        buffer.put(name, () -> value);
        return this;
    }

    public Statement apply(Statement base, Description description) {
        return new EnvironmentVariablesStatement(base, buffer);
    }

    private static class EnvironmentVariablesStatement extends Statement {

        private final Statement baseStatement;
        private final Map<String, Supplier<String>> buffer;
        private Map<String, String> originalVariables;

        EnvironmentVariablesStatement(Statement baseStatement, Map<String, Supplier<String>> buffer) {
            this.baseStatement = baseStatement;
            this.buffer = buffer;
        }

        public void evaluate() throws Throwable {
            saveCurrentState();

            try {
                copyVariablesFromBufferToEnvMap();
                baseStatement.evaluate();
            } finally {
                restoreOriginalVariables();
            }
        }

        private void saveCurrentState() {
            originalVariables = new HashMap<>(System.getenv());
        }

        private void copyVariablesFromBufferToEnvMap() {
            buffer.forEach((name, valueSupplier) -> {
                final String value = valueSupplier.get();
                writeVariableToEnvMap(name, value);
            });
        }

        private void restoreOriginalVariables() {
            restoreVariables(getEditableMapOfVariables());
            restoreVariables(getTheCaseInsensitiveEnvironment());
        }

        void restoreVariables(Map<String, String> variables) {
            if (variables != null) {
                variables.clear();
                variables.putAll(originalVariables);
            }
        }

        private static void writeVariableToEnvMap(String name, String value) {
            set(getEditableMapOfVariables(), name, value);
            set(getTheCaseInsensitiveEnvironment(), name, value);
        }

        private static Map<String, String> getEditableMapOfVariables() {
            final Class<?> classOfMap = System.getenv().getClass();

            try {
                return (Map<String, String>) getFieldValue(classOfMap, System.getenv(), "m");
            } catch (IllegalAccessException var2) {
                throw new RuntimeException("Rule cannot access the field 'm' of the map System.getenv().", var2);
            } catch (NoSuchFieldException var3) {
                throw new RuntimeException("Rule expects System.getenv() to have a field 'm' but it has not.", var3);
            }
        }

        private static Map<String, String> getTheCaseInsensitiveEnvironment() {
            try {
                final Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");

                return (Map<String, String>) getFieldValue(processEnvironment, null, "theCaseInsensitiveEnvironment");
            } catch (ClassNotFoundException var1) {
                throw new RuntimeException("Rule expects the existence of the class java.lang.ProcessEnvironment but it does not exist.", var1);
            } catch (IllegalAccessException var2) {
                throw new RuntimeException("Rule cannot access the static field 'theCaseInsensitiveEnvironment' of the class java.lang.ProcessEnvironment.", var2);
            } catch (NoSuchFieldException var3) {
                return null;
            }
        }

        private static Object getFieldValue(Class<?> klass, Object object, String name) throws NoSuchFieldException, IllegalAccessException {
            final Field field = klass.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        }

        private static void set(Map<String, String> variables, String name, String value) {
            if (variables != null) {
                if (value == null) {
                    variables.remove(name);
                } else {
                    variables.put(name, value);
                }
            }
        }
    }
}
