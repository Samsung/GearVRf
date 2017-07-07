/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "bullet_gvr_utils.h"

#include <BulletCollision/CollisionShapes/btShapeHull.h>

namespace gvr {

btCollisionShape *convertCollider2CollisionShape(Collider *collider) {
    btCollisionShape *shape = NULL;

    if (collider->shape_type() == COLLIDER_SHAPE_BOX) {
        return convertBoxCollider2CollisionShape(static_cast<BoxCollider *>(collider));
    } else if (collider->shape_type() == COLLIDER_SHAPE_SPHERE) {
        return convertSphereCollider2CollisionShape(static_cast<SphereCollider *>(collider));
    } else if (collider->shape_type() == COLLIDER_SHAPE_MESH) {
        return convertMeshCollider2CollisionShape(static_cast<MeshCollider *>(collider));
    }

    return NULL;
}

btCollisionShape *convertSphereCollider2CollisionShape(SphereCollider *collider) {
    btCollisionShape *shape = NULL;

    if (collider != NULL) {
        shape = new btSphereShape(btScalar(collider->get_radius()));
    }

    return shape;
}

btCollisionShape *convertBoxCollider2CollisionShape(BoxCollider *collider) {
    btCollisionShape *shape = NULL;

    if (collider != NULL) {
        shape = new btBoxShape(btVector3(collider->get_half_extents().x,
                                         collider->get_half_extents().y,
                                         collider->get_half_extents().z));
    }

    return shape;
}

btCollisionShape *convertMeshCollider2CollisionShape(MeshCollider *collider) {
    btCollisionShape *shape = NULL;

    if (collider != NULL) {
        shape = createConvexHullShapeFromMesh(collider->mesh());
    }

    return shape;
}

btConvexHullShape *createConvexHullShapeFromMesh(Mesh *mesh) {
    btConvexHullShape *hull_shape = NULL;

    if (mesh != NULL) {
        btConvexHullShape *initial_hull_shape = NULL;
        btShapeHull *hull_shape_optimizer = NULL;
        unsigned short vertex_index;

        initial_hull_shape = new btConvexHullShape();

        for (int i = 0; i < mesh->indices().size(); i++) {
            vertex_index = mesh->indices()[i];

            btVector3 vertex(mesh->vertices()[vertex_index].x,
                             mesh->vertices()[vertex_index].y,
                             mesh->vertices()[vertex_index].z);

            initial_hull_shape->addPoint(vertex);
        }

        btScalar margin(initial_hull_shape->getMargin());
        hull_shape_optimizer = new btShapeHull(initial_hull_shape);
        hull_shape_optimizer->buildHull(margin);

        hull_shape = new btConvexHullShape(
                (btScalar *) hull_shape_optimizer->getVertexPointer(),
                hull_shape_optimizer->numVertices());
    } else {
        LOGD("createConvexHullShapeFromMesh(): NULL mesh object");
    }

    return hull_shape;
}

btTransform convertTransform2btTransform(const Transform *t) {
    btQuaternion rotation(t->rotation_x(), t->rotation_y(), t->rotation_z(), t->rotation_w());

    btVector3 position(t->position_x(), t->position_y(), t->position_z());

    btTransform transform(rotation, position);

    return transform;
}

void convertBtTransform2Transform(btTransform bulletTransform, Transform *transform) {
    btVector3 pos = bulletTransform.getOrigin();
    btQuaternion rot = bulletTransform.getRotation();

    transform->set_position(pos.getX(), pos.getY(), pos.getZ());
    transform->set_rotation(rot.getW(), rot.getX(), rot.getY(), rot.getZ());
}

}
