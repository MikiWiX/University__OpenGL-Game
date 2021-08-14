#version 450 core

const int MAX_LIGHTS = 10; //max number of lights
const int MAX_JOINTS = 50; //max joints allowed in a skeleton
const int MAX_WEIGHTS = 3; //max number of joints that can affect a vertex

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;
in vec3 tangent;
// if animated
in ivec3 in_jointIndices;
in vec3 in_weights;

out vec2 pass_textureCoordinates;
out vec3 toLightVector[MAX_LIGHTS];
out vec3 toCameraVector;
out vec3 surfaceNormal;
// if shadows
out vec4 shadowCoords;
/* out float visibility; */

//matrices BASIC
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;
//(if shadows)
uniform mat4 toShadowMapSpace;
//matrices PRODUCTS
const mat4 modelViewMatrix = viewMatrix*transformationMatrix; // = viewMatrix * transformationMatrix;
const mat4 MVPMatrix = projectionMatrix*modelViewMatrix; // = projectionMatrix * viewMatrix * transformationMatrix;
//(if shadows)
const mat4 modelToShadowSpaceMatrix = toShadowMapSpace*transformationMatrix; // = toShadowMapSpace * transformationMatrix;
//if shaodws
uniform float shadowDistance;
uniform float transitionWidth;
uniform float transitionOffset;
//boolean flags
uniform float useNormalMap;
//lights
uniform vec3 lightPositionEyeSpace[MAX_LIGHTS];
//texture
uniform float numberOfRows;
uniform vec2 offset;
//if animated
uniform float isAnimated;
uniform mat4 jointTransforms[MAX_JOINTS];

/*
const float density = 0;
const float gradient = 5.0;

uniform vec4 plane;
*/

void main(void){

	/* gl_ClipDistance[0] = dot(worldPosition, plane); */
    vec4 positionRelativeToCam = modelViewMatrix * vec4(position,1.0);

    if(isAnimated>0.5) {
        vec4 totalLocalPos = vec4(0.0);
        vec4 totalNormal = vec4(0.0);
        for(int i=0;i<MAX_WEIGHTS;i++){
            vec4 localPosition = jointTransforms[in_jointIndices[i]] * vec4(position, 1.0);
            totalLocalPos += localPosition * in_weights[i];

            vec4 worldNormal = jointTransforms[in_jointIndices[i]] * vec4(normal, 0.0);
            totalNormal += worldNormal * in_weights[i];
        }

        gl_Position = MVPMatrix * totalLocalPos;
        surfaceNormal = (modelViewMatrix * totalNormal).xyz;
        shadowCoords = modelToShadowSpaceMatrix * totalLocalPos;
    } else {
        gl_Position = MVPMatrix * vec4(position,1.0);
        surfaceNormal = (modelViewMatrix * vec4(normal,0.0)).xyz;
        shadowCoords = modelToShadowSpaceMatrix * vec4(position,1.0);
    }

	pass_textureCoordinates = (textureCoordinates/numberOfRows) + offset;

	/* matrix transforming to tangent space, */

	/* further calculations are conversion from eye space to tangent space - if model uses normal map.
	Otherwise just pass eyespace */

    if (useNormalMap > 0.5) {
        /* --- if normals --- */
        vec3 norm = normalize(surfaceNormal);
        vec3 tang = normalize((modelViewMatrix * vec4(tangent, 0.0)).xyz);     /* <- transformed eye space to tangent */
        vec3 bitang = normalize(cross(norm, tang));

        mat3 toTangentSpace = mat3(
            tang.x, bitang.x, norm.x,
            tang.y, bitang.y, norm.y,
            tang.z, bitang.z, norm.z
        );

        for(int i=0;i<MAX_LIGHTS;i++){
            toLightVector[i] = toTangentSpace * (lightPositionEyeSpace[i] - positionRelativeToCam.xyz);
        }
        toCameraVector = toTangentSpace * (-positionRelativeToCam.xyz);

    } else {

        for(int i=0;i<MAX_LIGHTS;i++){
            toLightVector[i] = lightPositionEyeSpace[i] - positionRelativeToCam.xyz;
        }
        toCameraVector = -positionRelativeToCam.xyz;
    }

    /* --- SHADOW CALCULATIONS --- */
    float distance = length(positionRelativeToCam.xyz);
    distance = distance - (shadowDistance - transitionWidth + transitionOffset);
    distance = distance / transitionWidth;
/* clmap 'normalize' our value to between A and B */
    shadowCoords.w = clamp(1.0-distance, 0.0, 1.0);

    /*
	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance*density),gradient));
	visibility = clamp(visibility,0.0,1.0);
    */
}