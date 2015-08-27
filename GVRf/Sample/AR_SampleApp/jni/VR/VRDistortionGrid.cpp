#include "VRDistortionGrid.h"
#include<vector>
#include "VRDeviceInfo.h"

using namespace android;
using namespace GVR;

VRDistortionGrid::VRDistortionGrid(int horizontal_count, int vertical_count, int screen_width, int screen_height, VRSurfaceType type, VRRenderPass pass)
{
	int m = horizontal_count;
	int n = vertical_count;

	std::vector<vec3> vertices;

	for(int j = 0; j < n; ++j)
	{
		float y = -1.0f + 2.0f * j / (n - 1);
		for(int i = 0; i < m; ++i)
		{
			float x = -1.0f + 2.0f * i / (m - 1);
			vertices.push_back(vec3(x, y, 0.0f));
		}
	}
	mesh_.set_vertices(vertices);

	std::vector<vec2> uvs;

	float aspect_ratio = 0.0f;
	
	if(type == VR_SURFACE_PORTRAIT) {
		aspect_ratio = static_cast<float>(screen_height) / static_cast<float>(screen_width/2) ;

	} else {
		 aspect_ratio = static_cast<float>(screen_height) / static_cast<float>(screen_width / 2); //static_cast<float>(screen_height) / static_cast<float>(screen_width / 2);

	}
	
	vec2 scale_in(1.0f, 1.0f * aspect_ratio);
	vec2 scale(0.5f, 0.5f / aspect_ratio);

	vec2 lens_center;
	float lens_center_offset;
	vec2 vertex;
	for(std::vector<vec3>::iterator it = vertices.begin(); it != vertices.end(); ++it)
	{
		
		if(pass == VR_RENDERPASS_LEFT) {
			lens_center_offset = VRDeviceInfo::lens_center();
		}else {
			lens_center_offset = -VRDeviceInfo::lens_center();			
		}
		if(type == VR_SURFACE_PORTRAIT) {
			vertex.x = -it->y;
			vertex.y = it->x;

			lens_center.x = lens_center_offset;
			lens_center.y = 0.0f;
		} else {
			vertex.x = it->x;	
			vertex.y = it->y;

			lens_center.x = lens_center_offset;
			lens_center.y = 0.0f;
		}
		
		vec2 theta = (vertex - lens_center) * scale_in;
		float r_sq = theta.x * theta.x + theta.y * theta.y;
		vec2 rvector = theta * VRDeviceInfo::scaling_function2(r_sq) / VRDeviceInfo::distortion_scale();
		vec2 val = (rvector + lens_center) * scale + vec2(0.5f, 0.5f);
		uvs.push_back(val);
	}

	mesh_.set_uvs(uvs);

	std::vector<unsigned short> triangles;

	for(int j = 0; j < n - 1; ++j)
	{
		for(int i = 0; i < m - 1; ++i)
		{
			triangles.push_back( (i    ) + (j    ) * m);
			triangles.push_back( (i + 1) + (j    ) * m);
			triangles.push_back( (i    ) + (j + 1) * m);
			triangles.push_back( (i + 1) + (j    ) * m);
			triangles.push_back( (i + 1) + (j + 1) * m);
			triangles.push_back( (i    ) + (j + 1) * m);
		}
	}
	mesh_.set_triangles(triangles);


	std::vector<vec3> quad_vertices;
	std::vector<vec2> quad_uvs;
	std::vector<unsigned short> quad_triangles;


	quad_vertices.push_back(vec3(-1.0f, -1.0f, 0.0f));
	quad_vertices.push_back(vec3(1.0f, -1.0f, 0.0f));
	quad_vertices.push_back(vec3(1.0f, 1.0f, 0.0f));
	quad_vertices.push_back(vec3(-1.0f, 1.0f, 0.0f));

	quad_uvs.push_back(vec2(0.0f, 0.0f));
	quad_uvs.push_back(vec2(1.0f, 0.0f));
	quad_uvs.push_back(vec2(1.0f, 1.0f));
	quad_uvs.push_back(vec2(0.0f, 1.0f));

	quad_triangles.push_back(0);
	quad_triangles.push_back(1);
	quad_triangles.push_back(3);
	quad_triangles.push_back(2);

	quad_.set_vertices(quad_vertices);
	quad_.set_uvs(quad_uvs);
	quad_.set_triangles(quad_triangles);
}

VRDistortionGrid::~VRDistortionGrid()
{

}
