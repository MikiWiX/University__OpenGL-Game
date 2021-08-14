package customEntities.dataStructures.AlienBlocks;

import customEntities.Alien;
import renderEngine.GameMain;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import static customEntities.dataStructures.AlienBlocks.PixelClassicAlienBlock.CurrentAction.*;
import static customEntities.dataStructures.AlienBlocks.PixelClassicAlienBlock.MoveDirection.*;

public class PixelClassicAlienBlock extends AlienBlock{

    public PixelClassicAlienBlock(Alien[][] alienArray, Vector3f position, float[] spacing, AlienBlock.AlienBlockPoint point) {
        super(alienArray, position, spacing, point);
        this.lastPosY = arrayBotLPos.y;
        this.currI = 0;
        this.currJ = 0;
    }

    public PixelClassicAlienBlock(Alien[][] alienArray, Vector3f position, float width, float height, AlienBlock.AlienBlockPoint point) {
        super(alienArray, position, width, height, point);
        this.lastPosY = arrayBotLPos.y;
        this.currI = 0;
        this.currJ = 0;
    }


    private boolean columnIsEmpty(int columnIndex) {
        for (int i=0; i<alienArray.length; i++){
            if(alienArray[i][columnIndex] != null){
                return false;
            }
        }
        return true;
    }

    private void dropEmptyBorderColumns(){
        if(alienArray.length >0 && alienArray[0].length >0){

            if(columnIsEmpty(0)){

                arraySize[1] -= 1;
                arraySize[2] = arraySize[0]*arraySize[1];

                Alien[][] tmpAlienArray = new Alien[arraySize[0]][arraySize[1]];
                for(int i=0; i<arraySize[0]; i++){
                    for(int j=0; j<arraySize[1]; j++){
                        tmpAlienArray[i][j] = alienArray[i][j+1];
                    }
                }

                alienArray = tmpAlienArray;
                calculateWidth();
                this.arrayBotLPos.x += spacing[0];

                initPositions();
                //dropEmptyBorderColumns();

                currJ = (currJ == 0) ? 0 : currJ-1;

            } else if (columnIsEmpty(arraySize[1]-1)) {

                arraySize[1] -= 1;
                arraySize[2] = arraySize[0]*arraySize[1];

                Alien[][] tmpAlienArray = new Alien[arraySize[0]][arraySize[1]];
                for(int i=0; i<arraySize[0]; i++){
                    for(int j=0; j<arraySize[1]; j++){
                        tmpAlienArray[i][j] = alienArray[i][j];
                    }
                }

                alienArray = tmpAlienArray;
                calculateWidth();

                initPositions();
                //dropEmptyBorderColumns();
            }
        }
    }

    private boolean rowIsEmpty(int rowIndex) {
        for (int i=0; i<alienArray[0].length; i++){
            if(alienArray[rowIndex][i] != null){
                return false;
            }
        }
        return true;
    }

    private void dropEmptyBorderRows() {
        if(alienArray.length >0){

            if(rowIsEmpty(0)){

                arraySize[0] -= 1;
                arraySize[2] = arraySize[0]*arraySize[1];

                Alien[][] tmpAlienArray = new Alien[arraySize[0]][arraySize[1]];
                for(int i=0; i<arraySize[0]; i++){
                    for(int j=0; j<arraySize[1]; j++){
                        tmpAlienArray[i][j] = alienArray[i+1][j];
                    }
                }

                alienArray = tmpAlienArray;
                calculateHeight();
                this.arrayBotLPos.y += spacing[1];
                this.lastPosY += spacing[1];

                initPositions();
                //dropEmptyBorderRows();

                currI = (currI == 0) ? 0 : currI-1;

            } else if (rowIsEmpty(arraySize[0]-1)) {

                arraySize[0] -= 1;
                arraySize[2] = arraySize[0]*arraySize[1];

                Alien[][] tmpAlienArray = new Alien[arraySize[0]][arraySize[1]];
                for(int i=0; i<arraySize[0]; i++){
                    for(int j=0; j<arraySize[1]; j++){
                        tmpAlienArray[i][j] = alienArray[i][j];
                    }
                }

                alienArray = tmpAlienArray;
                calculateHeight();

                initPositions();
                //dropEmptyBorderRows();
            }
        }
    }

    @Override
    public void forEachEntity(AlienBlockCallback callback) {
        super.forEachEntity(callback);
        dropEmptyBorderColumns();
        dropEmptyBorderRows();
    }

    enum CurrentAction {
        ZIGZAG,
        MOVE_TO;
    }
    enum MoveDirection {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        NONE
    }

    private CurrentAction action = ZIGZAG;
    private MoveDirection horizontalMovement = RIGHT;

    private float lastPosY;
    private char stackTrace = 0;

    public float  stepSideLength=0.3f, minX=-100, maxX=100, stepDownLength=2;
    public Vector3f originPos;
    private float stepRate = 0.005f;
    private float timePassed = 0;
    private int currI , currJ;
    private float jumpTime = 0;

    public void setMovement(float stepSideLength, float minX, float maxX, float stepDownLength){
        this.stepSideLength = stepSideLength;
        this.minX = minX;
        this.maxX = maxX;
        this.stepDownLength = stepDownLength;
    }

    public void setStepRate(float stepRate){
        this.stepRate = stepRate;
    }

    private boolean firstMove = true;
    public void update() {
        if(firstMove){
            firstMove = false;
            jumpToSide(stepSideLength, minX, maxX);
        }
        switch(action){
            case ZIGZAG:
                updateAliens();
                break;
            case MOVE_TO:
                if(jumpTime <= 0){
                    action = ZIGZAG;
                    horizontalMovement = RIGHT;
                    arrayBotLPos = originPos;
                }
                jumpTime -= GameMain.getFrameRenderTime();
                forEachEntity((alien, i, j) -> {
                    alien.setPosition(new Vector3f(arrayBotLPos.x + alienOffsets[i][j][0], arrayBotLPos.y + alienOffsets[i][j][1], arrayBotLPos.z));
                    return true;
                });
        }

        forEachEntity((alien, i, j) -> {
            //alien.setPosition(new Vector3f(arrayBotLPos.x + alienOffsets[i][j][0], arrayBotLPos.y + alienOffsets[i][j][1], arrayBotLPos.z));
            alien.update();
            return true;
        });
    }

    private void updateAliens(){
        timePassed += GameMain.getFrameRenderTime();
        int jumps = (int) (timePassed/stepRate);
        timePassed = timePassed % stepRate;

        loop(jumps);
    }

    private void loop(int jumps) {
        if(arraySize[0] <= 0 || arraySize[1] <= 0){
            return;
        }
        for(int i=currI; i<arraySize[0]; i++){
            for(int j=currJ; j<arraySize[1]; j++){
                if(alienArray[i][j] != null){
                    if(jumps > 0){
                        jumps -= 1;
                        alienArray[i][j].setPosition(new Vector3f(arrayBotLPos.x + alienOffsets[i][j][0], arrayBotLPos.y + alienOffsets[i][j][1], arrayBotLPos.z));
                        if (alienArray[i][j].getModels().size()>1){
                            int cm = alienArray[i][j].getCurrentModelIndex();
                            if(++cm >= alienArray[i][j].getModels().size()) { cm = 0; }
                            alienArray[i][j].pickModel(cm);
                        }
                    } else {
                        currI = i;
                        currJ = j;
                        return;
                    }
                }
            }
            currJ=0;
        }
        currI = 0;
        jumpBlock(stepSideLength, minX, maxX, stepDownLength);
        loop(jumps);
    }

    private void jumpBlock(float stepSideLength, float minX, float maxX, float stepDownLength) {
        if(arrayBotLPos.x >= maxX - width){
            horizontalMovement = LEFT;
            arrayBotLPos.y -= stepDownLength;
        } else if (arrayBotLPos.x <= minX) {
            horizontalMovement = RIGHT;
            arrayBotLPos.y -= stepDownLength;
        }
        jumpToSide(stepSideLength, minX, maxX);
    }

    private void jumpToSide(float stepSideLength, float minX, float maxX){
        if(horizontalMovement == LEFT){
            arrayBotLPos.x -= stepSideLength;
        } else if (horizontalMovement == RIGHT){
            arrayBotLPos.x += stepSideLength;
        }
    }

    public void resetPos(Vector3f target, AlienBlock.AlienBlockPoint type) {
        action = MOVE_TO;
        originPos = calculatePositionFrom(target, type);
        jumpTime = 3;
    }

    public void resetPos(Vector3f target, AlienBlock.AlienBlockPoint type, float time) {
        action = MOVE_TO;
        originPos = calculatePositionFrom(target, type);
        jumpTime = time;
    }

}
