#version 450 core

in vec2 textureCoordinates1;
in vec2 textureCoordinates2;
in float blend;

out vec4 out_colour;

uniform sampler2D texture;

void main(void){

    vec4 colour1 = texture(texture, textureCoordinates1);
    vec4 colour2 = texture(texture, textureCoordinates2);

	out_colour = mix(colour1, colour2, blend);

}