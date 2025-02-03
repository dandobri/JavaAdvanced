package info.kgeorgiy.ja.dobris.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {
    // NOTE: int?
    private static final String nullHash = "00000000";
    static int readingFile(InputStream inputStream) {
        int size = 1024;
        byte[] b = new byte[size];
        int bSize;
        int hash = 0;
        try {
            while((bSize = inputStream.read(b)) != -1) {
                for(int i = 0; i < bSize; i++) {
                    hash = (hash + (b[i] & 0xff));
                    hash = (hash + (hash << 10));
                    hash ^= (hash >>> 6);
                }
            }
            hash = (hash + (hash << 3));
            hash ^= (hash >>> 11);
            hash = (hash + (hash << 15));
        } catch (IOException e) {
            System.err.println("You have an error in reading some part of file");
            return 0;
        } catch (SecurityException e) {
            System.err.println("You have a security exception: " + e);
            return 0;
        } catch (InvalidPathException e) {
            System.err.println("You have an invalid path exception: " + e);
            return 0;
        }
        return hash;
    }

    public static void main(String[] args) {
        if(args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Your command line format is not does not match");
        } else {
            Path inPath;
            Path outPath;
            try {
                inPath = Paths.get(args[0]);
                outPath = Paths.get(args[1]);
                try {
                    if(outPath.getParent() != null) {
                        Files.createDirectories(outPath.getParent());
                    }
                } catch (IOException e) {
                    System.err.println("You can not create this directory: " + e);
                    return;
                } catch (SecurityException e) {
                    System.err.println("You have a security exception: " + e);
                    return;
                }
            } catch (InvalidPathException e) {
                System.err.println("You can not create path: " + e);
                return;
            } catch (SecurityException e) {
                System.err.println("You have a security exception: " + e);
                return;
            }
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(String.valueOf(inPath)), StandardCharsets.UTF_8))){
                try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(String.valueOf(outPath)), StandardCharsets.UTF_8))){
                    try {
                        String str = reader.readLine();
                        while (str != null) {
                            Path file = null;
                            try {
                                file = Paths.get(str);
                            } catch (InvalidPathException e) {
                                System.err.println("You have an invalid path exception: " + e);
                            } catch (SecurityException e) {
                                System.err.println("You have a security exception: " + e);
                            }
                            String result = nullHash;
                            try (InputStream inputstream = new FileInputStream(String.valueOf(file))) {
                                int hash = readingFile(inputstream);
                                // NOTE: форматировать через String.format
                                result = String.format("%08x", hash);
                            } catch (SecurityException e) {
                                System.err.println("Write permission denied in file " + str + " : " + e);
                            } catch (FileNotFoundException e) {
                                System.err.println("File " + str + " can not open for reading: " + e);
                            } catch (IOException e) {
                                System.err.println("You have I/O error in file " + str + " : " + e);
                            } catch (InvalidPathException e) {
                                System.err.println("You have an invalid path exception: " + e);
                            }
                            writer.write(result + " " + str);
                            writer.newLine();
                            str = reader.readLine();
                        }
                    } catch (IOException e) {
                        System.err.println("You have an IOexception: " + e);
                    } catch (SecurityException e) {
                        System.err.println("You have a security exception: " + e);
                    }
                } catch (IOException e) {
                    System.err.println("You have an IOexception in writing file: " + e);
                } catch (SecurityException e) {
                    System.err.println("You have a security exception in writing file: " + e);
                }
            } catch (IOException e) {
                System.err.println("You have an IOexception in reading file: " + e);
            } catch (SecurityException e) {
                System.err.println("You have a security exception in reading file: " + e);
            }
        }
    }
}