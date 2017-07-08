precision mediump float;
attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texcoord;
uniform mat4 u_mvp;
uniform vec3 u_acceleration;
uniform float u_time;
uniform float u_particle_size;
uniform float u_size_change_rate;

varying float deltaTime;

void main() {

    deltaTime = u_time - a_texcoord.x;

    vec4 pos = a_position + vec4( (a_normal.x*deltaTime),
                                  (a_normal.y*deltaTime),
                                  (a_normal.z*deltaTime), 0) +
                         vec4( (0.5f * u_acceleration.x * deltaTime * deltaTime),
                               (0.5f * u_acceleration.y * deltaTime * deltaTime),
                               (0.5f * u_acceleration.z * deltaTime * deltaTime), 0);


    gl_Position = u_mvp * pos;

    gl_PointSize = (u_particle_size + u_size_change_rate * deltaTime);

    if ( gl_PointSize < 1.0f )
        gl_PointSize = 1.0f;

}

