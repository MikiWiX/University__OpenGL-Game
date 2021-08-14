package renderEngine.toolbox;

import java.io.*;

public class MyFile {
    private static final String FILE_SEPARATOR = "/";
    private String path;
    private String name;

    public MyFile(String path) {
        this.path = FILE_SEPARATOR + path;
        String[] dirs = path.split(FILE_SEPARATOR);
        this.name = dirs[dirs.length - 1];
    }

    public MyFile(String... paths) {
        this.path = "";
        String[] var5 = paths;
        int var4 = paths.length;

        for(int var3 = 0; var3 < var4; ++var3) {
            String part = var5[var3];
            this.path = this.path + FILE_SEPARATOR + part;
        }

        String[] dirs = this.path.split(FILE_SEPARATOR);
        this.name = dirs[dirs.length - 1];
    }

    public MyFile(MyFile file, String subFile) {
        this.path = file.path + FILE_SEPARATOR + subFile;
        this.name = subFile;
    }

    public MyFile(MyFile file, String... subFiles) {
        this.path = file.path;
        String[] var6 = subFiles;
        int var5 = subFiles.length;

        for(int var4 = 0; var4 < var5; ++var4) {
            String part = var6[var4];
            this.path = this.path + FILE_SEPARATOR + part;
        }

        String[] dirs = this.path.split(FILE_SEPARATOR);
        this.name = dirs[dirs.length - 1];
    }

    public String getPath() {
        return this.path;
    }

    public String toString() {
        return this.getPath();
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(System.getProperty("user.dir")+this.path);
        //return new FileInputStream("C:/Users/Miki/Desktop/JavaOpenGLProject/JOopenGL"+this.path);
        //return Class.class.getResourceAsStream(this.path);
    }

    public FileReader getFileReader() throws FileNotFoundException {
        return new FileReader(new File(this.path));
    }

    public BufferedReader getReader() throws Exception {
        try {
            //FileReader isr = this.getFileReader();
            InputStreamReader isr = new InputStreamReader(this.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            return reader;
        } catch (Exception e) {
            System.err.println("Couldn't get reader for " + this.path);
            throw e;
        }
    }

    public String getName() {
        return this.name;
    }
}
