precision mediump float;
attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texcoord;
uniform mat4 u_mvp;
uniform vec3 u_acceleration;
uniform float u_time;
uniform float u_particle_size;
uniform float u_size_change_rate;
uniform float u_particle_age;

varying float deltaTime;

void main() {

    deltaTime = u_time - a_texcoord.x;

    vec4 posn = a_position +
                vec4( (a_normal.x*deltaTime),
                      (a_normal.y*deltaTime),
                      (a_normal.z*deltaTime), 0) +
                vec4( (0.5 * u_acceleration.x * deltaTime * deltaTime),
                      (0.5 * u_acceleration.y * deltaTime * deltaTime),
                      (0.5 * u_acceleration.z * deltaTime * deltaTime), 0);

    gl_Position = u_mvp * posn;

    gl_PointSize = clamp((u_particle_size + u_size_change_rate * deltaTime), 0.1, 100.0);

    if ( deltaTime < 0.0 || deltaTime > u_particle_age)
    {
        //force the vertex to be clipped
        gl_Position = vec4(2.0,2.0,2.0,1.0);
    }

}

