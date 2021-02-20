import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonGenerator {

    public static String ASSETS_PATH = "assets/";
    public static String TEMPLATES_PATH = ASSETS_PATH + "templates/";
    public static File templates = new File(TEMPLATES_PATH);

    public static Scanner scanner = new Scanner(System.in);

    public static HashMap<String, ArrayList<String>> valueMap = new HashMap<String, ArrayList<String>>();

    public static void main(String[] args) throws IOException {
        System.out.println("Starting");
        for (File file : listFilesInDir(templates)) {
            getValuesFromFile(file);
        }

        /*
        for (File file : listFilesInDir(templates)) {
            generateFile(file);
        }
        */
    }

    public static ArrayList<File> listFilesInDir(File dir){
        ArrayList<File> files = new ArrayList<File>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                listFilesInDir(file);
            } else {
                files.add(file);
            }
        }
        return files;
    }

    public static void getValuesFromFile(File file) throws IOException {
        System.out.println(file.getName() + " : " + file.getPath());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String fileToWrite = "";
        String line;
        while ((line = br.readLine()) != null) {
            Pattern pattern = Pattern.compile("[{$][\\S][^{|}]*[}]");
            Matcher matcher = pattern.matcher(line);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void generateFile(File file) throws IOException {
        String path = file.getPath();
        path = path.replaceAll("templates", "generated");
        File directory = new File(path).getParentFile();
        if(!directory.exists()){
            directory.mkdir();
        }
        byte[] byteArrray = parseJson(file, path);
        try {
            FileOutputStream fos = new FileOutputStream(path, false);
            fos.write(byteArrray);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    public static byte[] parseJson(File file, String path) throws IOException {
        System.out.println(file.getName() + " : " + file.getPath());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String fileToWrite = "";
        String line;
        while ((line = br.readLine()) != null) {
            Pattern pattern = Pattern.compile("[{$][\\S][^{|}]*[}]");
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                System.out.print("Replace: " + matcher.group() + " with ");
                String replacement = scanner.nextLine();
                String tag = matcher.group()
                        .replace("{", "\\{")
                        .replace("}", "\\}")
                        .replace("$", "\\$");
                line = line.replaceAll(tag, replacement);
            }
            fileToWrite += line + "\n";
        }



        byte[] byteArrray;
        byteArrray = fileToWrite.getBytes(StandardCharsets.UTF_8);
        return byteArrray;
    }
}
