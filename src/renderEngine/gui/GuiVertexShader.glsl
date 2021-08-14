#version 450 core

in vec2 position;

out vec2 textureCords;

uniform mat4 transformationMatrix;

void main(void){

	gl_Position = transformationMatrix * vec4(position, 0.0, 1.0);
	textureCords = position * 0.5 + 0.5;
/* counts texture cords from positions */
}