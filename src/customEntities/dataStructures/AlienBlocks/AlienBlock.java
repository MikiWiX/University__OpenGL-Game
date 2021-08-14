package customEntities.dataStructures.AlienBlocks;

import customEntities.Alien;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AlienBlock {

    public enum AlienBlockPoint {
        DEFAULT,
        CENTER,
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        CENTER_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_CENTER,
        BOTTOM_LEFT,
        CENTER_LEFT
    }

    protected Alien[][] alienArray;
    protected float[][][] alienOffsets;
    //spacing[0] = X, spacing[1] = Y;
    protected float[] spacing;
    protected int[] arraySize;
    protected float width, height;
    protected Vector3f arrayBotLPos;

    protected AlienBlockPoint defaultPointType;

    protected volatile boolean safeEdit = true;

    public AlienBlock(Alien[][] alienArray, Vector3f position, float[] spacing, AlienBlockPoint point) {
        this.alienArray = alienArray;
        this.arraySize = new int[]{alienArray.length, alienArray[0].length, alienArray.length*alienArray[0].length };

        this.spacing = spacing;
        this.width = ( (arraySize[1] - 1) * spacing[0]);
        this.height = ( (arraySize[0] - 1) * spacing[1]);

        this.defaultPointType = point;
        this.arrayBotLPos = calculatePositionFrom(position, point);

        //this.lastPosY = arrayBotLPos.y;

        initPositions();
    }

    public AlienBlock(Alien[][] alienArray, Vector3f position, float width, float height, AlienBlockPoint point) {
        this.alienArray = alienArray;
        this.arraySize = new int[]{alienArray.length, alienArray[0].length, alienArray.length*alienArray[0].length };

        this.width = width;
        this.height = height;
        this.spacing = new float[] { width/(arraySize[1]-1), height/(arraySize[0]-1) };

        this.defaultPointType = point;
        this.arrayBotLPos = calculatePositionFrom(position, point);
        //this.lastPosY = arrayBotLPos.y;

        initPositions();
    }

    public void resizeByDimensions(float width, float height, AlienBlockPoint point){
        Vector3f tmpPoint = getPositionAt(point);
        this.width = width;
        this.height = height;
        this.spacing = new float[] { width/(arraySize[1]-1), height/(arraySize[0]-1) };
        this.arrayBotLPos = calculatePositionFrom(tmpPoint, point);
        initPositions();
    }
    public void resizeBySpacing(float[] spacing, AlienBlockPoint point){
        Vector3f tmpPoint = getPositionAt(point);
        this.spacing = spacing;
        calculateWidth();
        calculateHeight();
        this.arrayBotLPos = calculatePositionFrom(tmpPoint, point);
        initPositions();
    }

    protected void initPositions() {
        this.alienOffsets = new float[arraySize[0]][arraySize[1]][2];
        forEachEntity((alien, i, j) -> {
            alienOffsets[i][j][0] = j * spacing[0];
            alienOffsets[i][j][1] = i * spacing[1];
            alien.setPosition(new Vector3f(arrayBotLPos.x + alienOffsets[i][j][0], arrayBotLPos.y + alienOffsets[i][j][1], arrayBotLPos.z));
            return true;
        });
    }

    public void forEachEntity(AlienBlockCallback callback) {
        lock();
        arrayLoop(callback);
        unlock();
        removeAliens();
    }

    private void arrayLoop(AlienBlockCallback callback) {
        for(int i=0; i<arraySize[0]; i++){
            for(int j=0; j<arraySize[1]; j++){
                if(alienArray[i][j] != null){
                    if(!callback.call(alienArray[i][j], i, j)){
                        return;
                    }
                }
            }
        }
    }

    public void setDefaultPointType(AlienBlockPoint point){
        this.defaultPointType = point;
    }

    protected Vector3f calculatePositionFrom(Vector3f position, AlienBlockPoint point) {
        switch (point){
            case TOP_LEFT:
                return new Vector3f(position.x, position.y-height, position.z);
            case TOP_CENTER:
                return new Vector3f(position.x-(width/2), position.y-height, position.z);
            case TOP_RIGHT:
                return new Vector3f(position.x-width, position.y-height, position.z);
            case CENTER_RIGHT:
                return new Vector3f(position.x-width, position.y-(height/2), position.z);
            case BOTTOM_RIGHT:
                return new Vector3f(position.x-width, position.y, position.z);
            case BOTTOM_CENTER:
                return new Vector3f(position.x-(width/2), position.y, position.z);
            case BOTTOM_LEFT:
                return new Vector3f(position.x, position.y, position.z);
            case CENTER_LEFT:
                return new Vector3f(position.x, position.y-(height/2), position.z);
            default:
                return new Vector3f(position.x-(width/2), position.y-(height/2), position.z);
        }
    }

    private Vector3f getPositionAt(AlienBlockPoint point){
        switch (point){
            case CENTER:
                return new Vector3f(arrayBotLPos.x+(width/2), arrayBotLPos.y+(height/2), arrayBotLPos.z);
            case TOP_LEFT:
                return new Vector3f(arrayBotLPos.x, arrayBotLPos.y+height, arrayBotLPos.z);
            case TOP_CENTER:
                return new Vector3f(arrayBotLPos.x+(width/2), arrayBotLPos.y+height, arrayBotLPos.z);
            case TOP_RIGHT:
                return new Vector3f(arrayBotLPos.x+width, arrayBotLPos.y+height, arrayBotLPos.z);
            case CENTER_RIGHT:
                return new Vector3f(arrayBotLPos.x+width, arrayBotLPos.y+(height/2), arrayBotLPos.z);
            case BOTTOM_RIGHT:
                return new Vector3f(arrayBotLPos.x+width, arrayBotLPos.y, arrayBotLPos.z);
            case BOTTOM_CENTER:
                return new Vector3f(arrayBotLPos.x+(width/2), arrayBotLPos.y, arrayBotLPos.z);
            case BOTTOM_LEFT:
                return new Vector3f(arrayBotLPos.x, arrayBotLPos.y, arrayBotLPos.z);
            case CENTER_LEFT:
                return new Vector3f(arrayBotLPos.x, arrayBotLPos.y+(height/2), arrayBotLPos.z);
            default:
                return getPositionAt(this.defaultPointType);

        }
    }



    private List<int[]> emptySpace = new ArrayList<>();
    private List<int[]> toRemove = new ArrayList<>();

    private void removeAliens(){
        for(int[] i : toRemove){
            alienArray[i[0]][i[1]] = null;
        }
        toRemove.clear();
    }

    public void remove(Alien alien) {
        for(int i=0; i<alienArray.length; i++){
            for(int j=0; j<alienArray[i].length; j++){
                if(alienArray[i][j] == alien){
                    remove(i,j);
                    return;
                }
            }
        }
    }
    public void remove(int i, int j){remove( new int[]{i,j});}
    public void remove(int[] index2d) {
        if(safeEdit){
            alienArray[index2d[0]][index2d[1]] = null;
        } else {
            toRemove.add(index2d);
        }
    }

    public void add(Alien alien, int i, int j){add(alien, new int[]{i,j});}
    public void add(Alien alien, int[] index2d) {}

    private float getTop() {
        return arrayBotLPos.y+height;
    }
    protected float getLeft() {
        return arrayBotLPos.x;
    }
    protected float getBottom() {
        return arrayBotLPos.y;
    }
    protected float getRight() {
        return arrayBotLPos.x+width;
    }
    protected void calculateHeight() {
        this.height = ( (arraySize[0] - 1) * spacing[1]);
    }
    protected void calculateWidth() {
        this.width =  ( (arraySize[1] - 1) * spacing[0]);
    }

    public List<int[]> getEmptySpace() {
        return emptySpace;
    }

    public List<Alien> getAlienList() {
        return Arrays.stream(alienArray).flatMap(Arrays::stream).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Stream<Alien> getAlienStream() {
        return Arrays.stream(alienArray).flatMap(Arrays::stream).filter(Objects::nonNull);
    }

    public boolean isEmpty() {
        return (alienArray.length == 0 || getAlienList().size()==0);
    }

    public void lock(){
        while (!safeEdit) {
            Thread.onSpinWait();
        }
        safeEdit = false;
    }
    public void unlock(){
        safeEdit = true;
    }

    public float[] getSpacing() {
        return spacing;
    }
    public float getBlockWidth() {
        return width;
    }
    public float getBlockHeight() {
        return height;
    }
    public int[] getArraySize() {
        return arraySize;
    }

}
