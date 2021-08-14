package renderEngine.loaders.collada;

import renderEngine.toolbox.MyFile;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlParser {
    private static final Pattern DATA = Pattern.compile(">(.+?)<");
    private static final Pattern START_TAG = Pattern.compile("<(.+?)>");
    private static final Pattern ATTR_NAME = Pattern.compile("(.+?)=");
    private static final Pattern ATTR_VAL = Pattern.compile("\"(.+?)\"");
    private static final Pattern CLOSED = Pattern.compile("(</|/>)");

    public XmlParser() {
    }

    // file getter

    public static InputStream getInputStream(String path) throws FileNotFoundException {
        return new FileInputStream(System.getProperty("user.dir") +"/"+ path);
    }

    public static FileReader getFileReader(String path) throws FileNotFoundException {
        return new FileReader(new File(path));
    }

    public static BufferedReader getReader(String path) throws Exception {
        try {
            //FileReader isr = this.getFileReader();
            InputStreamReader isr = new InputStreamReader(getInputStream(path));
            BufferedReader reader = new BufferedReader(isr);
            return reader;
        } catch (Exception e) {
            System.err.println("Couldn't get reader for " + path);
            throw e;
        }
    }

    // actual XML parser below

    public static XmlNode loadXmlFile(String path) {
        BufferedReader reader = null;

        try {
            reader = getReader(path);
        } catch (Exception var4) {
            var4.printStackTrace();
            System.err.println("Can't find the XML file: " + path);
            System.exit(0);
            return null;
        }

        try {
            reader.readLine();
            XmlNode node = loadNode(reader);
            reader.close();
            return node;
        } catch (Exception var3) {
            var3.printStackTrace();
            System.err.println("Error with XML file format for: " + path);
            System.exit(0);
            return null;
        }
    }

    private static XmlNode loadNode(BufferedReader reader) throws Exception {
        String line = reader.readLine().trim();
        if (line.startsWith("</")) {
            return null;
        } else {
            String[] startTagParts = getStartTag(line).split(" ");
            XmlNode node = new XmlNode(startTagParts[0].replace("/", ""));
            addAttributes(startTagParts, node);
            addData(line, node);
            if (CLOSED.matcher(line).find()) {
                return node;
            } else {
                XmlNode child = null;

                while((child = loadNode(reader)) != null) {
                    node.addChild(child);
                }

                return node;
            }
        }
    }

    private static void addData(String line, XmlNode node) {
        Matcher matcher = DATA.matcher(line);
        if (matcher.find()) {
            node.setData(matcher.group(1));
        }

    }

    private static void addAttributes(String[] titleParts, XmlNode node) {
        for(int i = 1; i < titleParts.length; ++i) {
            if (titleParts[i].contains("=")) {
                addAttribute(titleParts[i], node);
            }
        }

    }

    private static void addAttribute(String attributeLine, XmlNode node) {
        Matcher nameMatch = ATTR_NAME.matcher(attributeLine);
        nameMatch.find();
        Matcher valMatch = ATTR_VAL.matcher(attributeLine);
        valMatch.find();
        node.addAttribute(nameMatch.group(1), valMatch.group(1));
    }

    private static String getStartTag(String line) {
        Matcher match = START_TAG.matcher(line);
        match.find();
        return match.group(1);
    }
}
