#version 450 core

in vec2 pass_textureCords;

out vec4 out_Color;

/* basic rendering */
uniform sampler2D fontAtlas;

uniform vec3 characterColor;
uniform float characterWidth;
uniform float characterEdge;

uniform vec3 borderColor;
uniform float borderWidth;
uniform float borderEdge;
/* for a glowing effect just do: less Width, more Border! */
/* for a shadow just move offset by a bit */
uniform vec2 borderOffset;

void main(void){


    /* ----------- Anti-Aliased Text ---------- */
    /* smoothstep = "if lexx than X -> alpha = 1, if more than Y -> alpha = 0, if between -> alpha = between." */
    float distance = 1.0 - texture(fontAtlas, pass_textureCords).a;
    float alpha = 1.0 - smoothstep(characterWidth, characterWidth+characterEdge, distance);

    /* --- same for border --- */
    float distance2 = 1.0 - texture(fontAtlas, pass_textureCords + borderOffset).a;
    float outlineAlpha = 1.0 - smoothstep(borderWidth, borderWidth+borderEdge, distance2);

    /* --- overall alpha --- space taken by letter + rest*border */
    /* also mix() takes two colors and mixes them by alpha */
    float overallAlpha = alpha + (1.0 - alpha) * outlineAlpha;
    vec3 overallColor = mix(borderColor, characterColor, alpha/overallAlpha);

    out_Color = vec4(overallColor, overallAlpha);


    /* ---------- Regular 1:1 renderEngine.text.renderEngine.text ---------- */
    /*
    out_Color = vec4(characterColor, texture(fontAtlas, pass_textureCords).a);
    */
}