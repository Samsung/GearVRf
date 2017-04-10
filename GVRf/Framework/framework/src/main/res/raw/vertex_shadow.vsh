#ifdef HAS_SHADOWS
mat4 temp_@LIGHTOUT = mat4(@LIGHTIN.sm0, @LIGHTIN.sm1, @LIGHTIN.sm2, @LIGHTIN.sm3);
@LIGHTOUT_shadow_position = temp_@LIGHTOUT * u_model * vertex.local_position;
#endif

