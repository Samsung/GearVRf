vec4 temp_@LIGHTOUT = @LIGHTIN.shadow_matrix * u_model * vertex.local_position;
@LIGHTOUT_shadow_position = temp_@LIGHTOUT;

