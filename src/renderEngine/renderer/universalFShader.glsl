#version 450 core

const int MAX_LIGHTS = 10; //max number of lights

in vec2 pass_textureCoordinates;
in vec3 toLightVector[MAX_LIGHTS];
in vec3 toCameraVector;
in vec4 shadowCoords;
in vec3 surfaceNormal;
/* in float visibility; */

out vec4 out_Color;

uniform sampler2D modelTexture;
uniform sampler2D normalMap;
uniform sampler2D specularMap;
uniform sampler2D shadowMap;

uniform float useTexture;
uniform float useNormalMap;
uniform float useSpecularMap;
uniform float useShadowMap;

uniform float tex_multiplier;
uniform float norm_multiplier;
uniform float spec_multiplier;

uniform vec3 lightColour[MAX_LIGHTS];
uniform vec3 attenuation[MAX_LIGHTS];
uniform float shineDamper;
uniform float reflectivity;
uniform float ambient;

/* uniform vec3 skyColour; */

// for shadow maps
uniform int mapSize;
uniform int pcfCount;
const float totalTexels = (pcfCount *2 +1.0) * (pcfCount *2 +1.0);

void main(void){

/* --- shadow calculations --- */

    float lightFactor = 1.0;
    if (useShadowMap > 0.5) {
        /* PCF - 'shadow multisampling' - testing other pixels! */
        float texelSize = 1.0/mapSize;
        float total = 0.0;

        for (int x=-pcfCount; x<=pcfCount; x++) {
            for (int y=-pcfCount; y<=pcfCount; y++) {
                float objectNearestLight = texture(shadowMap, shadowCoords.xy + vec2(x, y) * texelSize).r;
                    if(shadowCoords.z > objectNearestLight + 0.002) {
                        total += 1.0;
                }
            }
        }
        total /= totalTexels;

        lightFactor = 1.0 - (total * shadowCoords.w);
    }

/* --- normal calculations --- */

    vec3 unitNormal = vec3(0,0,0);
    if (useNormalMap > 0.5){
        vec4 normalMapValue = 2* texture(normalMap, pass_textureCoordinates * norm_multiplier) -1;
    	unitNormal = normalize(normalMapValue.rgb);
    } else {
        unitNormal = normalize(surfaceNormal);
    }


	vec3 unitVectorToCamera = normalize(toCameraVector);

/* --- light calculations --- */

	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);

	for(int i=0;i<MAX_LIGHTS;i++){
		float distance = length(toLightVector[i]);
		float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);
		vec3 unitLightVector = normalize(toLightVector[i]);
		float nDotl = dot(unitNormal,unitLightVector);
		float brightness = max(nDotl,0.0);
		vec3 lightDirection = -unitLightVector;
		vec3 reflectedLightDirection = reflect(lightDirection,unitNormal);
		float specularFactor = dot(reflectedLightDirection , unitVectorToCamera);
		specularFactor = max(specularFactor,0.0);
		float dampedFactor = pow(specularFactor,shineDamper);

		if (i==0) {
		totalDiffuse = totalDiffuse + (((brightness * lightColour[i])/attFactor) * lightFactor);
		totalSpecular = totalSpecular + (((dampedFactor * reflectivity * lightColour[i])/attFactor)*lightFactor);
		} else {
		totalDiffuse = totalDiffuse + ((brightness * lightColour[i])/attFactor);
		totalSpecular = totalSpecular + ((dampedFactor * reflectivity * lightColour[i])/attFactor);
		}
	}
	totalDiffuse = max(totalDiffuse, ambient);

    vec4 textureColour = vec4(0.8);
    if(useTexture > 0.5) {
        textureColour = texture(modelTexture, pass_textureCoordinates * tex_multiplier);
    }

/* -- opacity --
	if(textureColour.a<0.5){
		discard;
	}
	*/

/* --- for specular maps --- */

     if(useSpecularMap > 0.5) {
            vec4 mapInfo = texture(specularMap, pass_textureCoordinates * spec_multiplier);
            totalSpecular *= mapInfo.r;
     }

/* --- OUT --- */

     out_Color = vec4(totalDiffuse,1.0) * textureColour + vec4(totalSpecular,1.0);

	/* out_Color = mix(vec4(skyColour,1.0),out_Color, visibility); */
	/* out_Color = vec4(toCameraVector, 1.0); */
}