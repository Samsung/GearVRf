#ifdef HAS_SHADOWS
@LIGHTOUT.shadow_position = mat4(@LIGHTIN.sm0, @LIGHTIN.sm1, @LIGHTIN.sm2, @LIGHTIN.sm3) * u_model * vertex.local_position;
#endif

