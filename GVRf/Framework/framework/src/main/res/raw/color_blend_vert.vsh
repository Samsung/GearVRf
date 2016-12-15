        #version 300 es
        in vec3 a_position;
        in vec2 a_texcoord;
        out vec2 diffuse_coord;
        void main() {
         diffuse_coord = a_texcoord.xy;
          gl_Position = vec3(a_position, 1.0);
        }