precision mediump float;
uniform vec4 u_color;
uniform highp float u_particle_age;
uniform sampler2D tex0;
uniform float u_fade;

varying float deltaTime;

void main() {

    float opacity = 1.0;

    if ( u_fade == 1.0 )
    {
        opacity = 1.0 - (deltaTime / u_particle_age);
    }

    vec4 color = texture2D(tex0, gl_PointCoord);
    gl_FragColor = vec4(color.r * u_color.r * opacity * u_color.a, color.g * u_color.g * opacity * u_color.a,
    color.b * u_color.b * opacity * u_color.a, u_color.a * (color.a * opacity));

}
