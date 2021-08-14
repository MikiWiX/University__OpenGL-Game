#version 450 core

in vec2 textureCords;

out vec4 out_Color;

uniform sampler2D guiTexture;

void main(void){

	out_Color = texture(guiTexture,textureCords);

}