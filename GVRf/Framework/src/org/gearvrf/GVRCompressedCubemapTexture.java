package org.gearvrf;

public class GVRCompressedCubemapTexture extends GVRTexture
{
  // Compressed cubemap texture parameters
  public GVRCompressedCubemapTexture(GVRContext gvrContext, int internalFormat, int width,
          int height, int imageSize, byte[][] data, int[] dataOffset,
          GVRTextureParameters textureParameters) {
      super(gvrContext, NativeCompressedCubemapTexture.compressedTextureArrayConstructor(
              internalFormat, width, height, imageSize, data, dataOffset,
              textureParameters.getCurrentValuesArray()));
  }

  public GVRCompressedCubemapTexture(GVRContext gvrContext, int internalFormat, int width,
          int height, int imageSize, byte[][] data, int[] dataOffset) {
      this(gvrContext, internalFormat, width, height, imageSize, data, dataOffset,
              gvrContext.DEFAULT_TEXTURE_PARAMETERS);
  }
}

class NativeCompressedCubemapTexture {
  static native long compressedTextureArrayConstructor(int internalFormat,
          int width, int height, int imageSize, byte[][] data, int[] dataOffset,
          int[] textureParameterValues);
}