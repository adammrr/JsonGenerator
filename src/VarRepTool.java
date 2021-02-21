import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VarRepTool {

    public static String ASSETS_PATH = "assets/";
    public static String TEMPLATES_PATH = ASSETS_PATH + "templates/";
    public static File templates = new File(TEMPLATES_PATH);

    public static HashMap<String, ArrayList<String>> valueMap = new HashMap<>();

    public static void main(String[] args) throws IOException {

        ArrayList<File> templateFiles = FileHandler.listFilesInDir(templates);

        System.out.println("Starting Mass Variable Replacement Tool\n");
        System.out.println("> Find Variables and Assign Values");

        for (File file : templateFiles) {
            FileHandler.getVariablesFromFile(file, valueMap);
        }

        for (File file : templateFiles) {
            FileHandler.generateNewFiles(file, valueMap);
        }
    }
}
