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

#ifndef BULLET_RIGIDBODY_H_
#define BULLET_RIGIDBODY_H_

#include "../physics3d/physics_3drigidbody.h"

#include <BulletDynamics/Dynamics/btRigidBody.h>
#include <LinearMath/btMotionState.h>

namespace gvr {

class BulletRigidBody : public Physics3DRigidBody, btMotionState {
 public:
    BulletRigidBody();

    virtual ~BulletRigidBody();

    btRigidBody *getRigidBody() const {
        return mRigidBody;
    }

    void setMass(float mass) {
        mConstructionInfo.m_mass = btScalar(mass);
    }

    float getMass() {
        return mConstructionInfo.m_mass;
    }

    void setCenterOfMass(const Transform *t);

    void getRotation(float &w, float &x, float &y, float &z);

    void getTranslation(float &x, float &y, float &z);

    void getWorldTransform(btTransform &worldTrans) const;

    void setWorldTransform(const btTransform &worldTrans);

    void applyCentralForce(float x, float y, float z);

    void applyTorque(float x, float y, float z);

    void onAttach();

    void onDetach();

    float center_x() const;

    float center_y() const;

    float center_z() const;

    void set_center(float x, float y, float z);

    float rotation_w() const;

    float rotation_x() const;

    float rotation_y() const;

    float rotation_z() const;

    void set_rotation(float w, float x, float y, float z);

    float scale_x() const;

    float scale_y() const;

    float scale_z() const;

    void set_scale(float x, float y, float z);

    void setGravity(float x, float y, float z);

    void setDamping(float linear, float angular);

    void setLinearVelocity(float x, float y, float z);

    void setAngularVelocity(float x, float y, float z);

    void setAngularFactor(float x, float y, float z);

    void setLinearFactor(float x, float y, float z);

    void setFriction(float n);

    void setRestitution(float n);

    void setSleepingThresholds(float linear, float angular);

    void setCcdMotionThreshold(float n);

    void setCcdSweptSphereRadius(float n);

    void setContactProcessingThreshold(float n);

    void setIgnoreCollisionCheck(PhysicsRigidBody *collisionObj, bool ignore);

    void getGravity(float *v3) const;

    void getLinearVelocity(float *v3) const;

    void getAngularVelocity(float *v3) const;

    void getAngularFactor(float *v3) const;

    void getLinearFactor(float *v3) const;

    void getDamping(float &angular, float &linear) const;

    const float getFriction() const;

    const float getRestitution() const;

    const float getCcdMotionThreshold() const;

    const float getContactProcessingThreshold() const;

    const float getCcdSweptSphereRadius() const;

 private:
    void initialize();

    void finalize();

    void updateColisionShapeLocalScaling();

 private:
    btRigidBody *mRigidBody;
    btRigidBody::btRigidBodyConstructionInfo mConstructionInfo;
    btTransform m_centerOfMassOffset;
    btVector3 mScale;
};

}

#endif /* BULLET_RIGIDBODY_H_ */
