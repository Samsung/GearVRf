#ifndef VRDEVICE_INFO_H_
#define VRDEVICE_INFO_H_

namespace GVR {

class VRDeviceInfo
{
public:
	enum DeviceType
	{
		GalaxyS4
	};

	static float h_screen_size()
	{
		switch(CURRENT_DEVICE)
		{
		case GalaxyS4:
			return 0.1111f;
		}
	}

	static float lens_separation_distance()
	{
		switch(CURRENT_DEVICE)
		{
		case GalaxyS4:
			return 0.05f;
		}
	}

	static float k0()
	{
		switch(CURRENT_DEVICE)
		{
		case GalaxyS4:
			return 1.0f;
		}
	}

	static float k1()
	{
		switch(CURRENT_DEVICE)
		{
		case GalaxyS4:
				return 0.06f; //0.09f; // 0.06f - original value
		}
	}

	static float k2()
	{
		switch(CURRENT_DEVICE)
		{
		case GalaxyS4:
				return 0.07f; //0.09f; // 0.07f - original value
		}
	}

	static float k3()
	{
		switch(CURRENT_DEVICE)
		{
		case GalaxyS4:
			return 0.0f;
		}
	}

	/**
	 * a formula for lens distortion
	 */
	static float scaling_function(float r)
	{
		float r2 = r * r;
		return k0()
			 + k1() * r2
			 + k2() * r2 * r2
			 + k3() * r2 * r2 * r2;
	}

	static float scaling_function2(float r2)
	{
		return k0()
			 + k1() * r2
			 + k2() * r2 * r2
			 + k3() * r2 * r2 * r2;
	}

	static float lens_center()
	{
		return 1.0f - 2.0f * lens_separation_distance() / h_screen_size();
	}

	static float distortion_scale()
	{
		return scaling_function(-1.0f - lens_center());
	}

private:
	static const DeviceType CURRENT_DEVICE = GalaxyS4;
};
};

#endif /* DEVICE_INFO_H_ */

