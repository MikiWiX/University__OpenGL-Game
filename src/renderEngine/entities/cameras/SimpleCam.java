package renderEngine.entities.cameras;

import Input.Input;
import renderEngine.GameMain;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class SimpleCam extends Camera {

    public SimpleCam(Vector3f position, float dx, float dy, float dz) {
        super(position, dx, dy, dz);
    }

    @Override
    public void move() {
        ArrayList<Boolean> list = Input.getCameraMove();
        float ft = GameMain.getFrameRenderTime();
        if(list.get(0)) position.z -= 200f*ft;
        if(list.get(1)) position.z += 200f*ft;
        if(list.get(2)) position.x -= 200f*ft;
        if(list.get(3)) position.x += 200f*ft;
        if(list.get(4)) pitchDX -= 40f*ft;
        if(list.get(5)) pitchDX += 40f*ft;
        if(list.get(6)) yawDY -= 40f*ft;
        if(list.get(7)) yawDY += 40f*ft;
    }
}
