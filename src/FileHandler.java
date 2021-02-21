import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles Reading, Writing and Replacing Variables within files
 */
public class FileHandler {

    // Regular Expression for the Variable Tag. Set to '{$...}' by default
    private static final String REGEX = "[{$][\\S][^{|}]*[}]";
    private static final Pattern pattern = Pattern.compile(REGEX);

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Returns an ArrayList containing all files in Root Directory and its subdirectories.
     * @param dir Root Directory
     * @return ArrayList containing all Files
     */
    public static ArrayList<File> listFilesInDir(File dir){
        ArrayList<File> files = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                files = listFilesInDir(file);
            } else {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Finds variables that match {$_VAR} format within specified file, and adds to the specified map
     * @param file File to use
     * @param valueMap Map to append to
     * @throws IOException Throws if file is not Found, or issue reading the file (If file(s) are modified/deleted during runtime)
     */
    public static void getVariablesFromFile(File file, HashMap<String, ArrayList<String>> valueMap) throws IOException {
        System.out.println("\nReading File: " + file.getName() + "\n> " + file.getPath());

        Matcher matcher = pattern.matcher(file.getName());
        while(matcher.find()){
            String tag = matcher.group()
                    .replace("{", "\\{")
                    .replace("}", "\\}")
                    .replace("$", "\\$");
            if(!valueMap.containsKey(tag)){
                System.out.print("How many variants for " + matcher.group() + ": ");
                String variants = scanner.nextLine();
                int varientCount = Integer.parseInt(variants);
                ArrayList<String> arrayList = new ArrayList<>();

                for (int j = 0; j < varientCount; j++){
                    System.out.print("Replace: " + matcher.group() + " with ");
                    String replacement = scanner.nextLine();
                    arrayList.add(replacement);
                }
                valueMap.put(tag, arrayList);
            }
        }

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            matcher = pattern.matcher(line);
            while(matcher.find()){
                String tag = matcher.group()
                        .replace("{", "\\{")
                        .replace("}", "\\}")
                        .replace("$", "\\$");
                if(!valueMap.containsKey(tag)){
                    System.out.print("How many variants for " + matcher.group() + ": ");
                    String variants = scanner.nextLine();
                    int varientCount = Integer.parseInt(variants);
                    ArrayList<String> arrayList = new ArrayList<>();

                    for (int j = 0; j < varientCount; j++){
                        System.out.print("Replace: " + matcher.group() + " with ");
                        String replacement = scanner.nextLine();
                        arrayList.add(replacement);
                    }
                    valueMap.put(tag, arrayList);
                }
            }
        }
    }

    /**
     * Replaces the variables which match {$VAR} format with specified values
     * @param templateFile Template file with variables for replacement
     * @param valueMap Takes values from map and replaces variables in file with them
     * @throws IOException Throws if file is not Found, or issue reading the file (If file(s) are modified/deleted during runtime)
     */
    public static void generateNewFiles(File templateFile, HashMap<String, ArrayList<String>> valueMap) throws IOException {
        String fileName = templateFile.getName();
        String filePath = templateFile.getPath();

        System.out.println("\nTemplate: " + fileName + " " + filePath);

        List<String> keys = new ArrayList<>(valueMap.keySet());
        List<Map<String, String>> output = recursivelyFindCombinations(valueMap, keys, 0);

        // Runs through each combination
        for (Map<String, String> map : output){
            Matcher matcher = pattern.matcher(fileName);
            String newFileName = fileName;
            String newFilePath = filePath.replaceAll("templates", "generated");

            while(matcher.find()){
                String tag = matcher.group()
                        .replace("{", "\\{")
                        .replace("}", "\\}")
                        .replace("$", "\\$");
                newFileName = newFileName.replaceAll(tag, map.get(tag));
                newFilePath = newFilePath.replaceAll(tag, map.get(tag));
            }

            byte[] newFileBytes = replaceVarsInFile(templateFile, map);
            writeFile(newFilePath,newFileBytes);

            System.out.println("Wrote to: " + newFileName + " " + newFilePath);
        }
    }

    private static void writeFile(String path, byte[] byteArray){
        File directory = new File(path).getParentFile();
        if(!directory.exists()){
            if(!directory.mkdir()){
                System.err.println("Unable to create directory for file: " + path);
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(path, false);
            fos.write(byteArray);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] replaceVarsInFile(File file, Map<String, String> map) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder fileToWrite = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            Pattern pattern = Pattern.compile("[{$][\\S][^{|}]*[}]");
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                String tag = matcher.group()
                        .replace("{", "\\{")
                        .replace("}", "\\}")
                        .replace("$", "\\$");
                line = line.replaceAll(tag, map.get(tag));
            }
            fileToWrite.append(line).append("\n");
        }

        byte[] byteArray;
        byteArray = fileToWrite.toString().getBytes(StandardCharsets.UTF_8);
        return byteArray;
    }

    private static List<Map<String, String>> recursivelyFindCombinations(Map<String, ArrayList<String>> variables, List<String> keysInRecursionOrder, int depth) {
        String key = keysInRecursionOrder.get(depth);
        List<Map<String, String>> out = new ArrayList<>();

        if (depth == keysInRecursionOrder.size() - 1) {
            for (String value : variables.get(key)) {
                HashMap<String, String> kv = new HashMap<>();
                kv.put(key, value);
                out.add(kv);
            }
        } else {
            for (String value : variables.get(key)) {
                List<Map<String, String>> in = recursivelyFindCombinations(variables, keysInRecursionOrder, depth + 1);

                for (Map<String, String> kv : in) {
                    kv.put(key, value);
                    out.add(kv);
                }
            }
        }
        return out;
    }
}
