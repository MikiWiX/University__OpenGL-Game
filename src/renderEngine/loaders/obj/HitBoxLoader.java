package renderEngine.loaders.obj;


import renderEngine.physics.hitBox.*;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static renderEngine.loaders.obj.HitBoxLoader.LoadParams.*;

public class HitBoxLoader {

    protected enum LoadParams{
        VERTICES,
        NORMALS,
        APPROXIMATE
    }

    private static final float APPROX_VAL = 0.1f;

    public static HitBoxHULL loadOBJHitBoxHULL(String objFileName) {
        List<List<Vector3f>> plist = loadOBJHitBox(objFileName, VERTICES, NORMALS);
        return new HitBoxHULL(plist.get(0), plist.get(1));
    }
    public static HitBoxOBB loadOBJHitBoxOBB(String objFileName) {
        List<List<Vector3f>> plist = loadOBJHitBox(objFileName, VERTICES, NORMALS);
        return new HitBoxOBB(plist.get(0), plist.get(1));
    }
    public static HitBoxAABB_DYNAMIC generateAABB_DYNAMICFromModel(String objFileName){
        List<List<Vector3f>> plist = loadOBJHitBox(objFileName, VERTICES);
        return new HitBoxAABB_DYNAMIC(plist.get(0));
    }
    public static HitBoxAABB_FIXED generateAABB_FIXEDFromModel(String objFileName){
        List<List<Vector3f>> plist = loadOBJHitBox(objFileName, VERTICES);
        return new HitBoxAABB_FIXED(plist.get(0));
    }

    private static List<List<Vector3f>> loadOBJHitBox(String objFileName, LoadParams ... args) {
        FileReader isr = null;
        File objFile = new File(objFileName);
        try {
            isr = new FileReader(objFile);
        } catch (FileNotFoundException e) {
            System.err.println("File not found in res; don't use any extention");
        }
        assert isr != null;
        BufferedReader reader = new BufferedReader(isr);
        String line;

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();

        try {
            while (true) {
                line = reader.readLine();
                if (line.startsWith("v ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    vertices.add(vertex);
                } else if (line.startsWith("vn ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    normals.add(normal);
                } else if (line.startsWith("f ")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file");
        }

        List<List<Vector3f>> outPut = new ArrayList<>();
        boolean approximate = isInArray(LoadParams.APPROXIMATE, args);
        if(isInArray(LoadParams.VERTICES, args)){
            if(approximate){
                outPut.add(removeApproxDuplicates(vertices, APPROX_VAL));
            } else {
                outPut.add(removeDuplicates(vertices));
            }
        }
        if(isInArray(LoadParams.NORMALS, args)){
            if(approximate){
                outPut.add( normalize(removeApproxDuplicates(removeApproxOpposite(normals, APPROX_VAL), APPROX_VAL)) );
            } else {
                outPut.add( normalize(removeDuplicates(removeOpposite(normals)) ) );
            }
        }

        return outPut;
    }

    @SafeVarargs
    private static <T> boolean isInArray (T x, T ... args){
        for(T arg : args){
            if (x == arg){
                return true;
            }
        }
        return false;
    }

    public static List<List<Vector3f>> loadOBJVerticesAndNormals(String objFileName) {
        FileReader isr = null;
        File objFile = new File(objFileName);
        try {
            isr = new FileReader(objFile);
        } catch (FileNotFoundException e) {
            System.err.println("File not found in res; don't use any extention");
        }
        assert isr != null;
        BufferedReader reader = new BufferedReader(isr);
        String line;

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();

        try {
            while (true) {
                line = reader.readLine();
                if (line.startsWith("v ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    vertices.add(vertex);
                } else if (line.startsWith("vn ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    normals.add(normal);
                } else if (line.startsWith("f ")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file");
        }

        return Arrays.asList(removeApproxDuplicates(vertices, 0.1f), normalize(removeApproxDuplicates(removeApproxOpposite(normals, 0.1f), 0.1f) ));
    }
    public static List<Vector3f> loadOBJVertices(String objFileName){
        FileReader isr = null;
        File objFile = new File(objFileName);
        try {
            isr = new FileReader(objFile);
        } catch (FileNotFoundException e) {
            System.err.println("File not found in res; don't use any extention");
        }
        assert isr != null;
        BufferedReader reader = new BufferedReader(isr);
        String line;

        List<Vector3f> vertices = new ArrayList<>();

        try {
            while (true) {
                line = reader.readLine();
                if (line.startsWith("v ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    vertices.add(vertex);
                } else if (line.startsWith("f ")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file");
        }
        return removeApproxDuplicates(vertices, 0.1f);
    }

    private static List<Vector3f> removeOpposite(List<Vector3f> list){
        ListIterator it1 = list.listIterator();
        while(it1.hasNext()){
            int i = it1.nextIndex();
            Vector3f a = (Vector3f) it1.next();
            for (int j=i+1; j<list.size(); j++){
                Vector3f b = list.get(j);
                if (a.x == -b.x && a.y == -b.y && a.z == -b.z){
                    it1.remove();
                    break;
                }
            }
        }
        return list;
    }

    private static List<Vector3f> removeApproxOpposite(List<Vector3f> list, float approxVal) {
        float maxMultiplier = 1+approxVal;
        float minMultiplier = 1-approxVal;

        ListIterator it1 = list.listIterator();
        while(it1.hasNext()){
            int i = it1.nextIndex();
            Vector3f a = (Vector3f) it1.next();

            float aMaxX = a.x * maxMultiplier;
            float aMinX = a.x * minMultiplier;
            float aMaxY = a.y * maxMultiplier;
            float aMinY = a.y * minMultiplier;
            float aMaxZ = a.z * maxMultiplier;
            float aMinZ = a.z * minMultiplier;

            for (int j=i+1; j<list.size(); j++){
                Vector3f b = list.get(j);
                if (
                        -b.x > aMinX && -b.x < aMaxX &&
                                -b.y > aMinY && -b.x < aMaxY &&
                                -b.z > aMinZ && -b.z < aMaxZ
                ) {
                    it1.remove();
                    break;
                }
            }
        }
        return list;
    }

    private static List<Vector3f> removeDuplicates(List<Vector3f> list) {
        ListIterator it1 = list.listIterator();
        while(it1.hasNext()){
            int i = it1.nextIndex();
            Vector3f a = (Vector3f) it1.next();
            for (int j=i+1; j<list.size(); j++){
                Vector3f b = list.get(j);
                if (a.x == b.x && a.y == b.y && a.z == b.z){
                    it1.remove();
                    break;
                }
            }
        }
        return list;
    }

    private static List<Vector3f> removeApproxDuplicates(List<Vector3f> list, float approxVal) {
        float maxMultiplier = 1+approxVal;
        float minMultiplier = 1-approxVal;

        ListIterator it1 = list.listIterator();
        while(it1.hasNext()){
            int i = it1.nextIndex();
            Vector3f a = (Vector3f) it1.next();

            float aMaxX = a.x * maxMultiplier;
            float aMinX = a.x * minMultiplier;
            float aMaxY = a.y * maxMultiplier;
            float aMinY = a.y * minMultiplier;
            float aMaxZ = a.z * maxMultiplier;
            float aMinZ = a.z * minMultiplier;

            for (int j=i+1; j<list.size(); j++){
                Vector3f b = list.get(j);
                if (
                    b.x > aMinX && b.x < aMaxX &&
                    b.y > aMinY && b.x < aMaxY &&
                    b.z > aMinZ && b.z < aMaxZ
                ) {
                    it1.remove();
                    break;
                }
            }
        }
        return list;
    }

    private static List<Vector3f> normalize (List<Vector3f> list) {
        for (Vector3f norm : list) {
            norm.normalise();
        }
        return list;
    }

}
