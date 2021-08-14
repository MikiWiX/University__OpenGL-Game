#version 450 core

const int MAX_JOINTS = 50;
const int MAX_WEIGHTS = 3;

in vec3 in_position;
in ivec3 in_jointIndices;
in vec3 in_weights;

uniform mat4 mvpMatrix;

uniform float isAnimated;
uniform mat4 jointTransforms[MAX_JOINTS];

void main(void){

	if(isAnimated>0.5) {
		vec4 totalLocalPos = vec4(0.0);
		for(int i=0;i<MAX_WEIGHTS;i++){
			vec4 localPosition = jointTransforms[in_jointIndices[i]] * vec4(in_position, 1.0);
			totalLocalPos += localPosition * in_weights[i];
		}

		gl_Position = mvpMatrix * totalLocalPos;
	} else {
		gl_Position = mvpMatrix * vec4(in_position, 1.0);
	}
}