uniform mat4 u_mvp;

layout(location = 0) in vec3 a_position;

void main(){
 gl_Position =  u_mvp * vec4(a_position,1);
}