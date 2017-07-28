#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision mediump float;
layout ( location = 0 ) in vec3 a_position;
layout ( location = 2 ) in vec3 a_normal;
layout ( location = 1 ) in vec2 a_texcoord;

@MATERIAL_UNIFORMS

@MATRIX_UNIFORMS

layout ( location = 0 ) out float deltaTime;

void main() {

    deltaTime = u_time - a_texcoord.x;

    vec4 posn = vec4(a_position,1.0) +
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

