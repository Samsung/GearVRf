#pragma once
#include "vector.hpp"


namespace GVR {

template <typename T>
struct Matrix2 {
    Matrix2()
    {
        x.x = 1; x.y = 0;
        y.x = 0; y.y = 1;
    }
    Matrix2(const T* m)
    {
        x.x = m[0]; x.y = m[1];
        y.x = m[2]; y.y = m[3];
    }
    vec2 x;
    vec2 y;
};

template <typename T>
struct Matrix3 {
    Matrix3()
    {
        x.x = 1; x.y = 0; x.z = 0;
        y.x = 0; y.y = 1; y.z = 0;
        z.x = 0; z.y = 0; z.z = 1;
    }
    Matrix3(const T* m)
    {
        x.x = m[0]; x.y = m[1]; x.z = m[2];
        y.x = m[3]; y.y = m[4]; y.z = m[5];
        z.x = m[6]; z.y = m[7]; z.z = m[8];
    }
    Matrix3(vec3 x, vec3 y, vec3 z) : x(x), y(y), z(z)
    {
    }
    Matrix3 Transposed() const
    {
        Matrix3 m;
        m.x.x = x.x; m.x.y = y.x; m.x.z = z.x;
        m.y.x = x.y; m.y.y = y.y; m.y.z = z.y;
        m.z.x = x.z; m.z.y = y.z; m.z.z = z.z;
        return m;
    }
    const T* Pointer() const
    {
        return &x.x;
    }
    vec3 x;
    vec3 y;
    vec3 z;
};

template <typename T>
struct Matrix4 {
    Matrix4()
    {
        x.x = 1; x.y = 0; x.z = 0; x.w = 0;
        y.x = 0; y.y = 1; y.z = 0; y.w = 0;
        z.x = 0; z.y = 0; z.z = 1; z.w = 0;
        w.x = 0; w.y = 0; w.z = 0; w.w = 1;
    }
    Matrix4(const Matrix3<T>& m)
    {
        x.x = m.x.x; x.y = m.x.y; x.z = m.x.z; x.w = 0;
        y.x = m.y.x; y.y = m.y.y; y.z = m.y.z; y.w = 0;
        z.x = m.z.x; z.y = m.z.y; z.z = m.z.z; z.w = 0;
        w.x = 0; w.y = 0; w.z = 0; w.w = 1;
    }
    Matrix4(const T* m)
    {
        x.x = m[0];  x.y = m[1];  x.z = m[2];  x.w = m[3];
        y.x = m[4];  y.y = m[5];  y.z = m[6];  y.w = m[7];
        z.x = m[8];  z.y = m[9];  z.z = m[10]; z.w = m[11];
        w.x = m[12]; w.y = m[13]; w.z = m[14]; w.w = m[15];
    }
    Matrix4 operator * (const Matrix4& b) const
    {
        Matrix4 m;
        m.x.x = x.x * b.x.x + x.y * b.y.x + x.z * b.z.x + x.w * b.w.x;
        m.x.y = x.x * b.x.y + x.y * b.y.y + x.z * b.z.y + x.w * b.w.y;
        m.x.z = x.x * b.x.z + x.y * b.y.z + x.z * b.z.z + x.w * b.w.z;
        m.x.w = x.x * b.x.w + x.y * b.y.w + x.z * b.z.w + x.w * b.w.w;
        m.y.x = y.x * b.x.x + y.y * b.y.x + y.z * b.z.x + y.w * b.w.x;
        m.y.y = y.x * b.x.y + y.y * b.y.y + y.z * b.z.y + y.w * b.w.y;
        m.y.z = y.x * b.x.z + y.y * b.y.z + y.z * b.z.z + y.w * b.w.z;
        m.y.w = y.x * b.x.w + y.y * b.y.w + y.z * b.z.w + y.w * b.w.w;
        m.z.x = z.x * b.x.x + z.y * b.y.x + z.z * b.z.x + z.w * b.w.x;
        m.z.y = z.x * b.x.y + z.y * b.y.y + z.z * b.z.y + z.w * b.w.y;
        m.z.z = z.x * b.x.z + z.y * b.y.z + z.z * b.z.z + z.w * b.w.z;
        m.z.w = z.x * b.x.w + z.y * b.y.w + z.z * b.z.w + z.w * b.w.w;
        m.w.x = w.x * b.x.x + w.y * b.y.x + w.z * b.z.x + w.w * b.w.x;
        m.w.y = w.x * b.x.y + w.y * b.y.y + w.z * b.z.y + w.w * b.w.y;
        m.w.z = w.x * b.x.z + w.y * b.y.z + w.z * b.z.z + w.w * b.w.z;
        m.w.w = w.x * b.x.w + w.y * b.y.w + w.z * b.z.w + w.w * b.w.w;
        return m;
    }
    Vector4<T> operator * (const Vector4<T>& b) const
    {
        Vector4<T> v;
        v.x = x.x * b.x + x.y * b.y + x.z * b.z + x.w * b.w;
        v.y = y.x * b.x + y.y * b.y + y.z * b.z + y.w * b.w;
        v.z = z.x * b.x + z.y * b.y + z.z * b.z + z.w * b.w;
        v.w = w.x * b.x + w.y * b.y + w.z * b.z + w.w * b.w;
        return v;
    }
    Matrix4& operator *= (const Matrix4& b)
    {
        Matrix4 m = *this * b;
        return (*this = m);
    }
    bool operator == (const Matrix4& b) const
	{
    	return (
    			x.x == b.x.x && x.y == b.x.y && x.z == b.x.z && x.w == b.x.w &&
    			y.x == b.y.x && y.y == b.y.y && y.z == b.y.z && y.w == b.y.w &&
    			z.x == b.z.x && z.y == b.z.y && z.z == b.z.z && z.w == b.z.w &&
    			w.x == b.w.x && w.y == b.w.y && w.z == b.w.z && w.w == b.w.w
    			);
	}
    Matrix4 Transposed() const
    {
        Matrix4 m;
        m.x.x = x.x; m.x.y = y.x; m.x.z = z.x; m.x.w = w.x;
        m.y.x = x.y; m.y.y = y.y; m.y.z = z.y; m.y.w = w.y;
        m.z.x = x.z; m.z.y = y.z; m.z.z = z.z; m.z.w = w.z;
        m.w.x = x.w; m.w.y = y.w; m.w.z = z.w; m.w.w = w.w;
        return m;
    }
    Matrix3<T> ToMat3() const
    {
        Matrix3<T> m;
        m.x.x = x.x; m.y.x = y.x; m.z.x = z.x;
        m.x.y = x.y; m.y.y = y.y; m.z.y = z.y;
        m.x.z = x.z; m.y.z = y.z; m.z.z = z.z;
        return m;
    }
    Matrix4 Inverse() const
    {
//    	Matrix4 m;
//    	m.x.x = x.x; m.x.y = y.x; m.x.z = z.x; m.x.w = 0;
//    	m.y.x = x.y; m.y.y = y.y; m.y.z = z.y; m.y.w = 0;
//    	m.z.x = x.z; m.z.y = y.z; m.z.z = z.z; m.z.w = 0;
//    	m.w.x = -((m.x.x * w.x) + (m.y.x * w.y) + (m.z.x * w.z));
//    	m.w.y = -((m.x.y * w.x) + (m.y.y * w.y) + (m.z.y * w.z));
//    	m.w.z = -((m.x.z * w.x) + (m.y.z * w.y) + (m.z.z * w.z));
//    	m.w.w = 0;
//
//    	//inconplete
//    	return m;

    	//
    	// Inversion by Cramer's rule.  Code taken from an Intel publication
    	//
    	double Result[4][4];
    	double tmp[12]; /* temp array for pairs */
//    	double src[16]; /* array of transpose source matrix */
    	float const* src; /* array of transpose source matrix */
    	double det; /* determinant */
#define UINT unsigned int
    	/* transpose matrix */
//    	for (UINT i = 0; i < 4; i++)
//    	{
//    		src[i + 0 ] = (*this)[i][0];
//    		src[i + 4 ] = (*this)[i][1];
//    		src[i + 8 ] = (*this)[i][2];
//    		src[i + 12] = (*this)[i][3];
//    	}
    	//baek
    	src = Transposed().Pointer();

    	/* calculate pairs for first 8 elements (cofactors) */
    	tmp[0] = src[10] * src[15];
    	tmp[1] = src[11] * src[14];
    	tmp[2] = src[9] * src[15];
    	tmp[3] = src[11] * src[13];
    	tmp[4] = src[9] * src[14];
    	tmp[5] = src[10] * src[13];
    	tmp[6] = src[8] * src[15];
    	tmp[7] = src[11] * src[12];
    	tmp[8] = src[8] * src[14];
    	tmp[9] = src[10] * src[12];
    	tmp[10] = src[8] * src[13];
    	tmp[11] = src[9] * src[12];
    	/* calculate first 8 elements (cofactors) */
    	Result[0][0] = tmp[0]*src[5] + tmp[3]*src[6] + tmp[4]*src[7];
    	Result[0][0] -= tmp[1]*src[5] + tmp[2]*src[6] + tmp[5]*src[7];
    	Result[0][1] = tmp[1]*src[4] + tmp[6]*src[6] + tmp[9]*src[7];
    	Result[0][1] -= tmp[0]*src[4] + tmp[7]*src[6] + tmp[8]*src[7];
    	Result[0][2] = tmp[2]*src[4] + tmp[7]*src[5] + tmp[10]*src[7];
    	Result[0][2] -= tmp[3]*src[4] + tmp[6]*src[5] + tmp[11]*src[7];
    	Result[0][3] = tmp[5]*src[4] + tmp[8]*src[5] + tmp[11]*src[6];
    	Result[0][3] -= tmp[4]*src[4] + tmp[9]*src[5] + tmp[10]*src[6];
    	Result[1][0] = tmp[1]*src[1] + tmp[2]*src[2] + tmp[5]*src[3];
    	Result[1][0] -= tmp[0]*src[1] + tmp[3]*src[2] + tmp[4]*src[3];
    	Result[1][1] = tmp[0]*src[0] + tmp[7]*src[2] + tmp[8]*src[3];
    	Result[1][1] -= tmp[1]*src[0] + tmp[6]*src[2] + tmp[9]*src[3];
    	Result[1][2] = tmp[3]*src[0] + tmp[6]*src[1] + tmp[11]*src[3];
    	Result[1][2] -= tmp[2]*src[0] + tmp[7]*src[1] + tmp[10]*src[3];
    	Result[1][3] = tmp[4]*src[0] + tmp[9]*src[1] + tmp[10]*src[2];
    	Result[1][3] -= tmp[5]*src[0] + tmp[8]*src[1] + tmp[11]*src[2];
    	/* calculate pairs for second 8 elements (cofactors) */
    	tmp[0] = src[2]*src[7];
    	tmp[1] = src[3]*src[6];
    	tmp[2] = src[1]*src[7];
    	tmp[3] = src[3]*src[5];
    	tmp[4] = src[1]*src[6];
    	tmp[5] = src[2]*src[5];

    	tmp[6] = src[0]*src[7];
    	tmp[7] = src[3]*src[4];
    	tmp[8] = src[0]*src[6];
    	tmp[9] = src[2]*src[4];
    	tmp[10] = src[0]*src[5];
    	tmp[11] = src[1]*src[4];
    	/* calculate second 8 elements (cofactors) */
    	Result[2][0] = tmp[0]*src[13] + tmp[3]*src[14] + tmp[4]*src[15];
    	Result[2][0] -= tmp[1]*src[13] + tmp[2]*src[14] + tmp[5]*src[15];
    	Result[2][1] = tmp[1]*src[12] + tmp[6]*src[14] + tmp[9]*src[15];
    	Result[2][1] -= tmp[0]*src[12] + tmp[7]*src[14] + tmp[8]*src[15];
    	Result[2][2] = tmp[2]*src[12] + tmp[7]*src[13] + tmp[10]*src[15];
    	Result[2][2] -= tmp[3]*src[12] + tmp[6]*src[13] + tmp[11]*src[15];
    	Result[2][3] = tmp[5]*src[12] + tmp[8]*src[13] + tmp[11]*src[14];
    	Result[2][3] -= tmp[4]*src[12] + tmp[9]*src[13] + tmp[10]*src[14];
    	Result[3][0] = tmp[2]*src[10] + tmp[5]*src[11] + tmp[1]*src[9];
    	Result[3][0] -= tmp[4]*src[11] + tmp[0]*src[9] + tmp[3]*src[10];
    	Result[3][1] = tmp[8]*src[11] + tmp[0]*src[8] + tmp[7]*src[10];
    	Result[3][1] -= tmp[6]*src[10] + tmp[9]*src[11] + tmp[1]*src[8];
    	Result[3][2] = tmp[6]*src[9] + tmp[11]*src[11] + tmp[3]*src[8];
    	Result[3][2] -= tmp[10]*src[11] + tmp[2]*src[8] + tmp[7]*src[9];
    	Result[3][3] = tmp[10]*src[10] + tmp[4]*src[8] + tmp[9]*src[9];
    	Result[3][3] -= tmp[8]*src[9] + tmp[11]*src[10] + tmp[5]*src[8];
    	/* calculate determinant */
    	det=src[0]*Result[0][0]+src[1]*Result[0][1]+src[2]*Result[0][2]+src[3]*Result[0][3];
    	/* calculate matrix inverse */
    	det = 1.0f / det;

    	Matrix4 FloatResult;
    	float* fmat = (float*)FloatResult.Pointer();
    	for (UINT i = 0; i < 4; i++)
    	{
    		for (UINT j = 0; j < 4; j++)
    		{
//    			FloatResult[i][j] = float(Result[i][j] * det);
				//baek
    			fmat[i*4+j] = float(Result[i][j] * det);
    		}
    	}

#undef UINT
    	return FloatResult;

    	//
    	// Inversion by LU decomposition, alternate implementation
    	//
    	/*int i, j, k;

    	    for (i = 1; i < 4; i++)
    	    {
    	        _Entries[0][i] /= _Entries[0][0];
    	    }

    	    for (i = 1; i < 4; i++)
    	    {
    	        for (j = i; j < 4; j++)
    	        {
    	            float sum = 0.0;
    	            for (k = 0; k < i; k++)
    	            {
    	                sum += _Entries[j][k] * _Entries[k][i];
    	            }
    	            _Entries[j][i] -= sum;
    	        }
    	        if (i == 4-1) continue;
    	        for (j=i+1; j < 4; j++)
    	        {
    	            float sum = 0.0;
    	            for (int k = 0; k < i; k++)
    	                sum += _Entries[i][k]*_Entries[k][j];
    	            _Entries[i][j] =
    	               (_Entries[i][j]-sum) / _Entries[i][i];
    	        }
    	    }

    	    //
    	    // Invert L
    	    //
    	    for ( i = 0; i < 4; i++ )
    	    {
    	        for ( int j = i; j < 4; j++ )
    	        {
    	            float x = 1.0;
    	            if ( i != j )
    	            {
    	                x = 0.0;
    	                for ( int k = i; k < j; k++ )
    	                    x -= _Entries[j][k]*_Entries[k][i];
    	            }
    	            _Entries[j][i] = x / _Entries[j][j];
    	        }
    	    }

    	    //
    	    // Invert U
    	    //
    	    for ( i = 0; i < 4; i++ )
    	    {
    	        for ( j = i; j < 4; j++ )
    	        {
    	            if ( i == j ) continue;
    	            float sum = 0.0;
    	            for ( int k = i; k < j; k++ )
    	                sum += _Entries[k][j]*( (i==k) ? 1.0f : _Entries[i][k] );
    	            _Entries[i][j] = -sum;
    	        }
    	    }

    	    //
    	    // Final Inversion
    	    //
    	    for ( i = 0; i < 4; i++ )
    	    {
    	        for ( int j = 0; j < 4; j++ )
    	        {
    	            float sum = 0.0;
    	            for ( int k = ((i>j)?i:j); k < 4; k++ )
    	                sum += ((j==k)?1.0f:_Entries[j][k])*_Entries[k][i];
    	            _Entries[j][i] = sum;
    	        }
    	    }*/
    }
    const T* Pointer() const
    {
        return &x.x;
    }

	static void AffineInverse(Matrix4<T> &m){
		//float Result[16];
		//memcpy(Result, m, 16 * sizeof(float));

		vec4 translation = vec4(-m.w.x, -m.w.y, -m.w.z, m.w.w);	

		m.w.x = 0.0f;
		m.w.y = 0.0f;
		m.w.z = 0.0f;
		m.w.w = 1.0f;

		m = m.Transposed();
		
//		matrixTranspose( Result);

	//	vec4 translation = vec4(-m[12],-m[13],-m[14],m[15]);
		vec4 translation_;
		translation_.x = m.x.x*translation.x + m.x.y*translation.y  +m.x.z*translation.z + m.x.w*translation.w;
		translation_.y = m.y.x*translation.x + m.y.y*translation.y  + m.y.z*translation.z +m.y.w*translation.w;
		translation_.z = m.z.x*translation.x +  m.z.y*translation.y  +  m.z.z*translation.z + m.z.w*translation.w;
		translation_.w = m.w.x*translation.x + m.w.y*translation.y  + m.w.z*translation.z +m.w.w*translation.w;
		
		

		m.w = translation_;
		//Result[12] = translation_.x;  Result[13] = translation_.y;  Result[14] =translation_.z;  Result[15] = translation_.w; 

	//	memcpy(m, Result, 16 * sizeof(float));

	}		
    static Matrix4<T> Identity()
    {
        return Matrix4();
    }
    static Matrix4<T> Zeros()
	{
    	Matrix4 m;
    	m.x.x = 0; m.x.y = 0; m.x.z = 0; m.x.w = 0;
    	m.y.x = 0; m.y.y = 0; m.y.z = 0; m.y.w = 0;
    	m.z.x = 0; m.z.y = 0; m.z.z = 0; m.z.w = 0;
    	m.w.x = 0; m.w.y = 0; m.w.z = 0; m.w.w = 0;
    	return m;
	}

	    void AddTranslation(const Vector3<T> & v) {

		float *m = (float*)this->Pointer();
		
		for (int i = 0; i < 4; i++)
		{
			m[12+i] += m[i]*v.x + m[4+i]*v.y + m[8+i]*v.z;
		}
	
	    }
    static Matrix4<T> Translate(const Vector3<T>& v)
    {
        Matrix4 m;
        m.x.x = 1; m.x.y = 0; m.x.z = 0; m.x.w = 0;
        m.y.x = 0; m.y.y = 1; m.y.z = 0; m.y.w = 0;
        m.z.x = 0; m.z.y = 0; m.z.z = 1; m.z.w = 0;
        m.w.x = v.x; m.w.y = v.y; m.w.z = v.z; m.w.w = 1;
	        return m;
			//return m.Transposed();
    }
    static Matrix4<T> Translate(T x, T y, T z)
    {
        Matrix4 m;
        m.x.x = 1; m.x.y = 0; m.x.z = 0; m.x.w = 0;
        m.y.x = 0; m.y.y = 1; m.y.z = 0; m.y.w = 0;
        m.z.x = 0; m.z.y = 0; m.z.z = 1; m.z.w = 0;
        m.w.x = x; m.w.y = y; m.w.z = z; m.w.w = 1;
		return m;
//	        return m.Transposed();
    }
    static Matrix4<T> Scale(T s)
    {
        Matrix4 m;
        m.x.x = s; m.x.y = 0; m.x.z = 0; m.x.w = 0;
        m.y.x = 0; m.y.y = s; m.y.z = 0; m.y.w = 0;
        m.z.x = 0; m.z.y = 0; m.z.z = s; m.z.w = 0;
        m.w.x = 0; m.w.y = 0; m.w.z = 0; m.w.w = 1;
        return m;
    }
    static Matrix4<T> Scale(T x, T y, T z)
    {
        Matrix4 m;
        m.x.x = x; m.x.y = 0; m.x.z = 0; m.x.w = 0;
        m.y.x = 0; m.y.y = y; m.y.z = 0; m.y.w = 0;
        m.z.x = 0; m.z.y = 0; m.z.z = z; m.z.w = 0;
        m.w.x = 0; m.w.y = 0; m.w.z = 0; m.w.w = 1;
        return m;
    }
    static Matrix4<T> Scale(const Vector3<T>& v)
    {
        Matrix4 m;
        m.x.x = v.x; m.x.y = 0; m.x.z = 0; m.x.w = 0;
        m.y.x = 0; m.y.y = v.y; m.y.z = 0; m.y.w = 0;
        m.z.x = 0; m.z.y = 0; m.z.z = v.z; m.z.w = 0;
        m.w.x = 0; m.w.y = 0; m.w.z = 0; m.w.w = 1;
        return m;
    }
    static Matrix4<T> Rotate(T degrees)
    {
        T radians = degrees * 3.14159f / 180.0f;
        T s = std::sin(radians);
        T c = std::cos(radians);

        Matrix4 m = Identity();
        m.x.x =  c; m.x.y = s;
        m.y.x = -s; m.y.y = c;
        return m;
    }
    static Matrix4<T> Rotate(T radians, const vec3& axis)
    {
        T s = std::sin(radians);
        T c = std::cos(radians);

        Matrix4 m = Identity();
        m.x.x = c + (1 - c) * axis.x * axis.x;
        m.x.y = (1 - c) * axis.x * axis.y - axis.z * s;
        m.x.z = (1 - c) * axis.x * axis.z + axis.y * s;
        m.y.x = (1 - c) * axis.x * axis.y + axis.z * s;
        m.y.y = c + (1 - c) * axis.y * axis.y;
        m.y.z = (1 - c) * axis.y * axis.z - axis.x * s;
        m.z.x = (1 - c) * axis.x * axis.z - axis.y * s;
        m.z.y = (1 - c) * axis.y * axis.z + axis.x * s;
        m.z.z = c + (1 - c) * axis.z * axis.z;
        return m;
    }
    static Matrix4<T> RotateX(T degrees) {
    	T radians = degrees * 3.14159f / 180.0f;
    	T s = std::sin(radians);
    	T c = std::cos(radians);
    	Matrix4 m;
    	m.x.x = 1; m.x.y = 0; m.x.z = 0; m.x.w = 0;
    	m.y.x = 0; m.y.y = c; m.y.z = s; m.y.w = 0;
    	m.z.x = 0; m.z.y =-s; m.z.z = c; m.z.w = 0;
    	m.w.x = 0; m.w.y = 0; m.w.z = 0; m.w.w = 1;
    	return m;
    }
    static Matrix4<T> RotateY(T degrees) {
    	T radians = degrees * 3.14159f / 180.0f;
    	T s = std::sin(radians);
    	T c = std::cos(radians);
    	Matrix4 m;
    	m.x.x = c; m.x.y = 0; m.x.z =-s; m.x.w = 0;
    	m.y.x = 0; m.y.y = 1; m.y.z = 0; m.y.w = 0;
    	m.z.x = s; m.z.y = 0; m.z.z = c; m.z.w = 0;
    	m.w.x = 0; m.w.y = 0; m.w.z = 0; m.w.w = 1;
    	return m;
    }
    static Matrix4<T> RotateZ(T degrees) {
    	T radians = degrees * 3.14159f / 180.0f;
    	T s = std::sin(radians);
    	T c = std::cos(radians);
    	Matrix4 m;
    	m.x.x = c; m.x.y = s; m.x.z = 0; m.x.w = 0;
    	m.y.x =-s; m.y.y = c; m.y.z = 0; m.y.w = 0;
    	m.z.x = 0; m.z.y = 0; m.z.z = 1; m.z.w = 0;
    	m.w.x = 0; m.w.y = 0; m.w.z = 0; m.w.w = 1;
    	return m;
    }
    static Matrix4<T> Ortho(T left, T right, T bottom, T top, T near, T far)
    {
        T a = 2.0f / (right - left);
        T b = 2.0f / (top - bottom);
        T c = -2.0f / (far - near);
        T tx = (right + left) / (right - left);
        T ty = (top + bottom) / (top - bottom);
        T tz = (far + near) / (far - near);
        Matrix4 m;
        m.x.x = a; m.x.y = 0; m.x.z = 0; m.x.w = tx;
        m.y.x = 0; m.y.y = b; m.y.z = 0; m.y.w = ty;
        m.z.x = 0; m.z.y = 0; m.z.z = c; m.z.w = tz;
        m.w.x = 0; m.w.y = 0; m.w.z = 0; m.w.w = 1;
        return m;
    }
	    static Matrix4<T> Perspective(T fovRadians, T aspect, T near, T far) {
	   Matrix4 mat;

	   float *m = (float*) mat.Pointer() ;
	   float f = 1.0f / (float) tan(fovRadians * 0.5f);
          float rangeReciprocal = 1.0f / (near - far);

	        m[  0] = f / aspect;
	        m[  1] = 0.0f;
	        m[  2] = 0.0f;
	        m[  3] = 0.0f;

	        m[  4] = 0.0f;
	        m[  5] = f;
	        m[  6] = 0.0f;
	        m[  7] = 0.0f;

	        m[  8] = 0.0f;
	        m[  9] = 0.0f;
	        m[  10] = (far + near) * rangeReciprocal;
	        m[  11] = -1.0f;

	        m[  12] = 0.0f;
	        m[  13] = 0.0f;
	        m[  14] = 2.0f * far * near * rangeReciprocal;
	        m[  15] = 0.0f;
			
		return mat;
		
		
	    }
	    static Matrix4<T> Frustum(T left, T right, T bottom, T top, T near, T far)
	    {

	        T a = 2 * near / (right - left);
	        T b = 2 * near / (top - bottom);
	        T c = (right + left) / (right - left);
	        T d = (top + bottom) / (top - bottom);
	        T e = - (far + near) / (far - near);
	        T f = -2 * far * near / (far - near);
	        Matrix4 m;
	        m.x.x = a; m.x.y = 0; m.x.z = 0; m.x.w = 0;
	        m.y.x = 0; m.y.y = b; m.y.z = 0; m.y.w = 0;
	        m.z.x = c; m.z.y = d; m.z.z = e; m.z.w = -1;
	        m.w.x = 0; m.w.y = 0; m.w.z = f; m.w.w = 1;
	        return m;
	    }

    static Matrix4<T> LookAt(const Vector3<T>& eye,
                             const Vector3<T>& target,
                             const Vector3<T>& up)
    {
	        Vector3<T> f = (target - eye).Normalized();
	        Vector3<T> s = f.Cross(up).Normalized();
	        Vector3<T> u = s.Cross(f).Normalized();

        Matrix4<T> m;
		 m.x.x = s.x;  m.x.y = u.x; m.x.z =  -f.x;  m.x.w = 0;
		 m.y.x = s.y;  m.y.y = u.y; m.y.z =  -f.y;  m.y.w = 0;
		 m.z.x = s.z;  m.z. y = u.z; m.z.z = -f.z;    m.z. w= 0;

		float *mat = (float*)m.Pointer();

        	for (int i=0 ; i<4 ; i++) {
            		mat[12 + i] += mat[i] * -eye.x + mat[4 + i] * -eye.y + mat[8 + i] * -eye.z;
        	}

        return m;
    }
    static Matrix4<T> LookAtWithVector(const Vector3<T>& eye,
                             	 	   const Vector3<T>& lookVector,
                             	 	   const Vector3<T>& up)
    {
        Vector3<T> z = lookVector.Normalized();
        Vector3<T> x = up.Cross(z).Normalized();
        Vector3<T> y = z.Cross(x).Normalized();

        Matrix4<T> m;
        m.x = Vector4<T>(x, 0);
        m.y = Vector4<T>(y, 0);
        m.z = Vector4<T>(z, 0);
        m.w = Vector4<T>(0, 0, 0, 1);

        Vector4<T> eyePrime = m * Vector4<T>(-eye, 1);
        m = m.Transposed();
        m.w = eyePrime;

        return m;
    }
    vec4 x;
    vec4 y;
    vec4 z;
    vec4 w;
};

typedef Matrix2<float> mat2;
typedef Matrix3<float> mat3;
typedef Matrix4<float> mat4;
}

