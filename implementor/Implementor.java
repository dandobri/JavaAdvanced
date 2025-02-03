package info.kgeorgiy.ja.dobris.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// :NOTE: main required
public class Implementor implements Impler {
    private final static String SEPARATOR = System.lineSeparator();
    private final static String INDENT = " ".repeat(4);

    public static void main(String[] args) throws ClassNotFoundException, ImplerException {
        if (args == null || args.length != 1) {
            System.err.println("Invalid command line arguments");
            return;
        }
        Implementor implementor = new Implementor();
        implementor.implement(Class.forName(args[0]), Path.of("__Test__Implementor__/test_CommandLine"));
    }


    public void createFile(Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
        } catch (IOException e) {
            System.err.println("You can not create this directory: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("You have a security exception: " + e.getMessage());
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (Modifier.isPrivate(token.getModifiers()) || Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Error implementing class");
        }
        try {
            Path path = Paths.get((root + "." + token.getPackageName() + "." + token.getSimpleName() + "Impl").replace(".", File.separator) + ".java").toAbsolutePath();
            createFile(path);
            Class<?> implementor = Class.forName(token.getTypeName());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(String.valueOf(path)))) {
                writer.write(declareClass(token));
                for (Method m : implementor.getMethods()) {
                    writer.write(writeMethod(m));
                }
                writer.write("}");
            } catch (IOException e) {
                System.err.println("Error in writing to file " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Can not found class by token and root " + e.getMessage());
        }
    }

    public String declareClass(Class<?> token) {
        return "package " + token.getPackageName() + ";" + SEPARATOR + "public class "
                + token.getSimpleName() + "Impl" + " implements "
                + token.getTypeName().replace("$", ".") + " {" + SEPARATOR;
    }

    public String writeMethod(Method m) {
        StringBuilder sb = new StringBuilder();
        // :NOTE: tabulation. 4 space
        sb.append(INDENT + "public ").append(m.getReturnType().getTypeName()).append(" ").append(m.getName()).append("(");
        // :NOTE: jstyle
        int i = 0;
        String[] funcList = new String[m.getParameterCount()];
        for (Class<?> type : m.getParameterTypes()) {
            funcList[i] = type.getTypeName().replace("$", ".") + " parameter" + i;
            i++;
            // :NOTE: join
        }
        sb.append(String.join(", ", funcList));
        return sb.append(") ").append("{").append(SEPARATOR).append(INDENT + INDENT + "return ")
                .append(returnDefault(m.getReturnType())).append(";").append(SEPARATOR).append(INDENT + "}")
                .append(SEPARATOR).toString();
    }

    public String returnDefault(Class<?> cla) {
        if (cla.equals(void.class)) {
            return "";
        } else if (cla.equals(boolean.class)) {
            return "false";
        } else if (cla.isPrimitive()) {
            return "0";
        } else {
            return "null";
        }
    }
}
