precision highp float;
out vec4 color;
in vec4 proj_position;

vec4 packFloatToVec4i(const float depth)
{
    const vec4 packFactors = vec4(256.0 * 256.0 * 256.0, 256.0 * 256.0, 256.0, 1.0);
    const vec4 bitMask = vec4(0.0, 1.0 / 256.0, 1.0 / 256.0, 1.0 / 256.0);
    vec4 c = vec4(fract(packFactors * depth));
    return c - c.xxyz * bitMask;
}


void main()
{
    float depth = proj_position.z / proj_position.w;
    depth = (depth + 1.0) / 2.0;
    color = packFloatToVec4i(depth);
}
