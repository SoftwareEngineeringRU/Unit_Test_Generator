import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class OutputGenerator {

    private ArrayList<File> outputFiles;
    private ArrayList<File> files;
    //private ArrayList<File> testFixtures;
    private ParseCPP parserCPP;
    private ParseInclude parserInclude;
    private static final Logger LOGGER = Logger.getLogger(OutputGenerator.class.getName());

    public OutputGenerator(ArrayList<File> files) {
        this.files = files;
        parserInclude = new ParseInclude(files.get(0).getAbsolutePath(), files);
        parserCPP = new ParseCPP(files.get(0).getAbsolutePath(), files);
        outputFiles = new ArrayList<>();
    }

    public void writeMakeFile() {
        ArrayList<String> objectList = new ArrayList<>();
        File makefile = new File("makefile");
        //file = new File(getDirectoryName());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(makefile))) {
            writer.write("all: executable");
            writer.write("\n\nOBJS =");

            for (File object : files) {
                String oFile = object.getName().split("\\.")[0] + ".o";
                //String fileNm = object.getName();
                //System.out.println("Hea:" + fileNm);
                objectList.add(oFile);
                writer.write(" " + oFile);
            }

            writer.write("\nCC = g++");
            writer.write("\nDEBUG = -g");
            writer.write("\nCFLAGS = -Wall -c $(DEBUG)");
            writer.write("\nLFLAGS = -Wall $(DEBUG)");
            writer.write("\n\nexecutable : $(OBJS)");
            writer.write("\n\t$(CC) $(LFLAGS) $(OBJS) -o executable");

            for (File input : files) {
                String oFile = input.getName().split("\\.")[0] + ".o";
                writer.write("\n\n" + oFile + " : ");
                ArrayList<String> dependencies = parserInclude.parse(input);

                for (String dependency : dependencies) {
                    writer.write(dependency);
                }
                writer.write("\n\t$(CC) $(CFLAGS) " + input.getName());
            }
            writer.write("\n\nclean :");
            writer.write("\n\t-rm *.o $(OBJS) executable");

            writer.flush();
            writer.close();

            outputFiles.add(makefile);
        } catch (Exception e) {
            System.out.println("Error generating makefile.");
        }
    }

    public void writeTestFixtures() {
        for (File input : files) {
            ArrayList<String> dependencies = parserInclude.parse(input);
            String className = input.getName().split("\\.")[0];

            File testFixture = new File(className + "Fixture.h");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFixture))) {
                writer.write("#include \"TestHarness.h\"\n");

                for (String header : dependencies) {
                    System.out.println(header);
                    writer.write("#include " + '"' + header + '"' + "\n");
                }

                String testName = input.getName().split("\\.")[0] + "Fixture()";
                //String obj = " object";
                writer.write("\nstruct " + className + "Fixture:testing::Test");
                writer.write("\n{");
                writer.write("\n\t" + className + " *comp;");
                writer.write("\n\n\t" + testName);
                writer.write("\n\t{");
                writer.write("\n\t\tcomp = new " + className + "();");
                writer.write("\n\t}");
                writer.write("\n\n\t~" + testName);
                writer.write("\n\t{");
                writer.write("\n\t\tdelete comp;");
                writer.write("\n\t}");
                writer.write("\n}");

                writer.flush();
                writer.close();

                outputFiles.add(testFixture);
                //testFixtures.add(file);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error generating test fixture for " + className);
            }
        }
    }

    public void writeUnitTests() {
        for (File input : files) {
            String className = input.getName().split("\\.")[0];
            String fixtureName = className + "Fixture";

            File unitTestFile = new File(className + "Test.cpp");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(unitTestFile))) {
                ArrayList<String> methodList = parserCPP.parse(input);

                writer.write("#include \"TestHarness.h\"");
                writer.write("\n#include \"" + fixtureName + ".h\"");

                for (String method : methodList) {
                    writer.write("\n\nTEST( " + className + "Tests, " + method + "_Test )");
                    writer.write("\n{");
                    writer.write("\n\n\t" + fixtureName + " f;");
                    writer.write("\n\t//Test Logic goes here");
                    writer.write("\n\n}");
                }

                writer.flush();
                writer.close();

                outputFiles.add(unitTestFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getDirectoryName() {
        String s = "";
        String filePath = files.get(0).getParent();
        String directoryName = filePath.replace("\\", "\\\\");
        return directoryName;
    }

    public ArrayList getOutputFiles() {
        System.out.println();
        for (File file : outputFiles) {
            System.out.println("File generated: " + file.getName());
        }

        return outputFiles;
    }

    private String getFileExtension(String fileName) {
        String extension = "NoExtension";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }
}