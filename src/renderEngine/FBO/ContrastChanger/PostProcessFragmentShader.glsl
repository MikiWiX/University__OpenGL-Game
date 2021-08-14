#version 450 core

in vec2 textureCords;

out vec4 out_Colour;

uniform sampler2D colourTexture;

uniform float brightness;
uniform float contrast;

void main(void){

    /* out_Colour = texture(colourTexture, textureCords).rbga; */

	out_Colour = texture(colourTexture, textureCords)*brightness;
	out_Colour.rgb = (out_Colour.rgb - 0.5)*(contrast) + 0.5;

}