precision highp float;
out vec4 color;
in vec4 v_vPosition;
vec4 packFloatToVec4i(const float depth)
{
    const vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);

    float r = depth;
    float g = fract(r * 256.0);
    float b = fract(g * 256.0);
    float a = fract(b * 256.0);
    vec4 c = vec4(r, g, b, a);
    //return c - (c.yzww * bias);
    return vec4(depth, depth, depth, 1.0);
}

void main()
{
 //   color = packFloatToVec4i(gl_FragCoord.z);

    float normalizedDistance = v_vPosition.z / v_vPosition.w;
    normalizedDistance = (normalizedDistance + 1.0) / 2.0;
    normalizedDistance += 0.0005;
    const vec4 packFactors = vec4( 256.0 * 256.0 * 256.0, 256.0 * 256.0 , 256.0 , 1.0);
    const vec4 bitMask = vec4(0.0 , 1.0 / 256.0 , 1.0 / 256.0 , 1.0 / 256.0);
    vec4 packedValue = vec4(fract(packFactors*normalizedDistance));
    packedValue -= packedValue.xxyz * bitMask;
    color = packedValue;
}
