package customEntities.dataStructures.AlienBlocks;

import customEntities.Alien;
import renderEngine.GameMain;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import static customEntities.dataStructures.AlienBlocks.SmoothClassicAlienBlock.CurrentAction.*;
import static customEntities.dataStructures.AlienBlocks.SmoothClassicAlienBlock.MoveDirection.*;

public class SmoothClassicAlienBlock extends AlienBlock {

    public SmoothClassicAlienBlock(Alien[][] alienArray, Vector3f position, float[] spacing, AlienBlockPoint point) {
        super(alienArray, position, spacing, point);
        this.lastPosY = arrayBotLPos.y;
    }

    public SmoothClassicAlienBlock(Alien[][] alienArray, Vector3f position, float width, float height, AlienBlockPoint point) {
        super(alienArray, position, width, height, point);
        this.lastPosY = arrayBotLPos.y;
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
        MOVE_TO
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
    private MoveDirection verticalMovement = NONE;

    private float lastPosY;
    private char stackTrace = 0;

    public float speed=30, minX=-100, maxX=100, stepDown=20;
    public Vector3f originPos;
    public float moveToSpeed=30;

    public void update() {
        switch(action){
            case ZIGZAG:
                zigZagMoveBlock(speed, minX, maxX, stepDown);
                break;
            case MOVE_TO:
                arrayBotLPos = moveTo(originPos, arrayBotLPos, moveToSpeed);
                if(arrayBotLPos.x == originPos.x && arrayBotLPos.y == originPos.y && arrayBotLPos.z == originPos.z){
                    action = ZIGZAG;
                    horizontalMovement = RIGHT;
                    verticalMovement = NONE;
                }
        }

        forEachEntity((alien, i, j) -> {
            alien.setPosition(new Vector3f(arrayBotLPos.x + alienOffsets[i][j][0], arrayBotLPos.y + alienOffsets[i][j][1], arrayBotLPos.z));
            alien.update();
            return true;
        });
    }

    public void resetPos(Vector3f target, AlienBlockPoint type) {
        action = MOVE_TO;
        originPos = calculatePositionFrom(target, type);
        moveToSpeed = speed;
    }
    public void resetPosWithSpeed(Vector3f target, AlienBlockPoint type, float speed){
        action = MOVE_TO;
        originPos = calculatePositionFrom(target, type);
        moveToSpeed = speed;
    }
    public void resetPosInTime(Vector3f target, AlienBlockPoint type, float time){
        action = MOVE_TO;
        originPos = calculatePositionFrom(target, type);

        Vector3f totalVector = Vector3f.sub(target, arrayBotLPos, null);
        float totalDistance = totalVector.length();
        moveToSpeed = totalDistance/time;
    }

    private Vector3f moveTo(Vector3f target, Vector3f from, float speed){
        Vector3f totalVector = Vector3f.sub(target, from, null);
        float totalDistance = totalVector.length();
        float ft = GameMain.getFrameRenderTime();
        float distance = speed * ft;
        float scale = distance/totalDistance;
        if(scale >=1){
            return target;
        } else {
            totalVector.scale(scale);
            return Vector3f.add(from, totalVector, null);
        }
    }

    private void zigZagMoveBlock(float speed, float minX, float maxX, float stepDown) {
        stackTrace = 0;
        float ft = GameMain.getFrameRenderTime();
        float distance = speed * ft;
        if (verticalMovement == DOWN) {
            moveDown(distance*2, minX, maxX, stepDown);
        } else if (horizontalMovement == LEFT){
            moveLeft(distance, minX, maxX, stepDown);
        } else if (horizontalMovement == RIGHT){
            moveRight(distance, minX, maxX, stepDown);
        }
    }

    private void moveRight(float distance, float minX, float maxX, float stepDown){
        if(stackTrace++ > 5) { return; }
        arrayBotLPos.x += distance;

        float xPos = getRight();
        if (xPos > maxX){
            float offset = xPos - maxX;
            arrayBotLPos.x = maxX - width;

            lastPosY = arrayBotLPos.y;
            moveDown(offset*2, minX, maxX, stepDown);
        }
    }
    private void moveLeft(float distance, float minX, float maxX, float stepDown){
        if(stackTrace++ > 5) { return; }
        arrayBotLPos.x -= distance;

        float xPos = getLeft();
        if (xPos < minX){
            float offset = minX - xPos;
            arrayBotLPos.x = minX;

            lastPosY = arrayBotLPos.y;
            moveDown(offset*2, minX, maxX, stepDown);
        }
    }
    private void moveDown(float distance, float minX, float maxX, float stepDown){
        if(stackTrace++ > 5) { return; }
        arrayBotLPos.y -= distance;
        verticalMovement = DOWN;

        float yPos = getBottom();
        if(yPos < lastPosY-stepDown){
            verticalMovement = NONE;

            float offset = (lastPosY-stepDown) - arrayBotLPos.y;
            arrayBotLPos.y = lastPosY-stepDown;

            if(horizontalMovement == LEFT) {
                horizontalMovement = RIGHT;
                moveRight(offset/2, minX, maxX, stepDown);
            } else if (horizontalMovement == RIGHT) {
                horizontalMovement = LEFT;
                moveLeft(offset/2, minX, maxX, stepDown);
            }
        }
    }

    public void setMovement(float speed, float minX, float maxX, float stepDown){
        this.speed = speed;
        this.minX = minX;
        this.maxX = maxX;
        this.stepDown = stepDown;
    }
}
