## Sample Model Hierarchy Loader GVRF App

This application demonstrates the functionality provided by the assimp integration of GVRF which enables it to load collada and obj 3D modelling files with complete scene hierarchies into GVRF.

The Sponza model included as asset in this app is public domain and was downloaded as obj from the Crytek website and converted into a collada model. [http://www.crytek.com/cryengine/cryengine3/downloads] 

The cube model was created using blender.

[Assumption]
All the 3D models which are being used to load using the loadScene(...) method, must have textures included as a part of the file. 3D models without an accompanying diffuse texture may not load correctly.
