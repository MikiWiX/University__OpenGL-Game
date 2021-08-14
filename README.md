# Java OpenGL Game
### University task
###### Java, OpenGL, GLSL
#### Running
Import libraries from 'libs' and 'depencies-archieves', open 'Project' folder in your IDE of choice and run.
#### Description
A simple 3D game 'Space Invaders' made using Java and LWJGL libraries.
It is a game engine on its own.
#### Side Notes
This is probably my biggest personal project so far, and I also had a lot of fun making it.  
Now huge thanks to Yotu.be channel ThinMatrix for his tutorials on creating this.  
Most of my code here follows those tutorials.  
- parts of it are copied without much change, like Particle Package,
- parts are significantly modified, like entity rendering system,
- a lot of code has been created by me from scrath like:
  - entire collision system that was not featured in ThinMatrix tutorials
  - entire modular level creation system using dedicated components
  - parallel loading screen, as well as parallel execution of physics and rendering on sesparate threads,
  - or of course all the models along with their movements

There is some junk data placed in GUI that indicate camera and ship positions, and tests memory management and clearing while recreating text textures.

Controls are assigned in a single Java Class, but here are current key bidings:
  - use arrows to move the camera sideways
  - use WASD to rotate camera
  - use JL to move your ship
  - press Space to fire a bullet

I gained a hell lot of knowledge (and liking) about Java while working on this project. I have also learned GLSL and how do Shaders work.

Textures used here are mostly free-to use or of my creation.

There you can find my most valuable sources of knowledge while working on this project:
![URL's](https://github.com/MikiWiX/University__OpenGL-Game/edit/main/LearningSources/URLs.txt)
https://github.com/MikiWiX/University__OpenGL-Game/edit/main/LearningSources/URLs.txt

Here is a sample of how it looks like:
![Sample Screenshot](https://github.com/MikiWiX/University__OpenGL-Game/blob/main/Sample.png)
