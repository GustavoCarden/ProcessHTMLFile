package processhtmlfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 *
 * @author GC014121
 */
public class ProcessHTMLFile {

    private static final String INITIAL_PATH = "filePath";
    private static boolean MODIFIED = false;
    private static boolean MODIFIED_LINE = false;

    private static void searchFilesInFolder(final File folder) throws IOException {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                searchFilesInFolder(fileEntry);
            } else {
                if (fileEntry.getAbsolutePath().contains(".jsp")) {
                    modifyContent(fileEntry.getAbsolutePath());
                }
            }
        }
    }

    private static void setBoolean(boolean flag) {
        MODIFIED = flag;
    }

    public static void setMODIFIED_LINE(boolean MODIFIED_LINE) {
        ProcessHTMLFile.MODIFIED_LINE = MODIFIED_LINE;
    }
    
    private static void modifyContent(String path) throws IOException {
        setBoolean(false);
        FileWriter fw = null;
        Writer writer = null;
        try {
            //Get File Content.
            StringBuilder contentBuilder = new StringBuilder();
            try ( Stream<String> stream = Files.lines(Paths.get(path), StandardCharsets.ISO_8859_1)) {
                stream.forEach(s -> {
                    setMODIFIED_LINE(false);
                    
                    //Eliminamos elementos src repetidos de tags <iframe>
                    if (s.contains("<iframe") && Utils.countMatches(s, "src=") > 1) {
                        int initialInd = s.indexOf("src=");
                        int lenght = 0;
                        for (int i = 0; i < 2; i++) {
                            do {
                                lenght++;
                            } while (s.charAt(initialInd + lenght) != '"');
                            lenght++;
                        }

                        String src = s.substring(initialInd, initialInd + lenght);
                        s = s.replaceFirst(src, "");

                        //Cambiamos el width y height
                        initialInd = s.indexOf("width=");
                        initialInd = initialInd + "width=".length();
                        src = s.substring(initialInd, initialInd + 3);
                        s = s.replaceFirst(src, "200");
                        
                        initialInd = s.indexOf("height=");
                        initialInd = initialInd + "height=".length();
                        src = s.substring(initialInd, initialInd + 3);
                        s = s.replaceFirst(src, "225");

                        contentBuilder.append(s).append("\n");
                        setBoolean(true);
                        setMODIFIED_LINE(true);
                    } 

                    //Agregamos Id a los elementos que solo tienen nombre.
                    if (s.contains("name=") && !s.contains("id=")) {
                        int initialInd = s.indexOf("name=");
                        int lenght = 0;
                        for (int i = 0; i < 2; i++) {
                            do {
                                lenght++;
                            } while (s.charAt(initialInd + lenght) != '"');
                            lenght++;
                        }

                        String name = s.substring(initialInd, initialInd + lenght);
                        String id = name.replace("name", "id");
                        s = s.replace(name, id + " " + name);

                        contentBuilder.append(s).append("\n");
                        setBoolean(true);
                        setMODIFIED_LINE(true);
                    } 
                    
                    if(!MODIFIED_LINE){
                        contentBuilder.append(s).append("\n");
                    }
                });
            }

            if (MODIFIED) {
                //Write Again in File.
                writer = new OutputStreamWriter(new FileOutputStream(new File(path)), StandardCharsets.ISO_8859_1);
                //fw = new FileWriter(path);
                writer.write(contentBuilder.toString());
                System.err.println("Modificando: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            if (fw != null) {
                fw.flush();
                fw.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.err.println("Empezando Proceso...");
        File projectDirectory = new File(INITIAL_PATH);
        if (projectDirectory.isDirectory()) {
            searchFilesInFolder(projectDirectory);
        }
        System.err.println("Ha concluido el Proceso...");
    }

}
