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
#include "bullet_world.h"
#include "bullet_rigidbody.h"
#include "bullet_gvr_utils.h"
#include "objects/scene_object.h"
#include "objects/components/sphere_collider.h"
#include "util/gvr_log.h"

#include <BulletCollision/CollisionDispatch/btCollisionObject.h>
#include <BulletCollision/CollisionShapes/btEmptyShape.h>
#include <LinearMath/btDefaultMotionState.h>
#include <LinearMath/btTransform.h>
#include <math.h>

namespace gvr {

BulletRigidBody::BulletRigidBody()
        : mConstructionInfo(btScalar(0.0f), nullptr, new btEmptyShape()),
          mRigidBody(new btRigidBody(mConstructionInfo)),
          m_centerOfMassOffset(btTransform::getIdentity()),
          mScale(1.0f, 1.0f, 1.0f),
          mSimType(SimulationType::DYNAMIC)
{
    mRigidBody->setUserPointer(this);
}

BulletRigidBody::BulletRigidBody(btRigidBody *rigidBody)
        : mConstructionInfo(btScalar(0.0f), nullptr, nullptr),
          mRigidBody(rigidBody),
          m_centerOfMassOffset(btTransform::getIdentity()),
          mScale(1.0f, 1.0f, 1.0f),
          mSimType(SimulationType::DYNAMIC)
{
    mRigidBody->setUserPointer(this);
}

BulletRigidBody::~BulletRigidBody() {
    finalize();
}

void BulletRigidBody::setSimulationType(PhysicsRigidBody::SimulationType type)
{
    mSimType = type;
    switch (type)
    {
        case SimulationType::DYNAMIC:
        mRigidBody->setCollisionFlags(mRigidBody->getCollisionFlags() &
                                      ~(btCollisionObject::CollisionFlags::CF_KINEMATIC_OBJECT |
                                        btCollisionObject::CollisionFlags::CF_STATIC_OBJECT));
        mRigidBody->setActivationState(ACTIVE_TAG);
        break;

        case SimulationType::STATIC:
        mRigidBody->setCollisionFlags(mRigidBody->getCollisionFlags() |
                                      btCollisionObject::CollisionFlags::CF_STATIC_OBJECT &
                                      ~btCollisionObject::CollisionFlags::CF_KINEMATIC_OBJECT);
        mRigidBody->setActivationState(ISLAND_SLEEPING);
        break;

        case SimulationType::KINEMATIC:
        mRigidBody->setCollisionFlags(mRigidBody->getCollisionFlags() |
                                       btCollisionObject::CollisionFlags::CF_KINEMATIC_OBJECT &
                                       ~btCollisionObject::CollisionFlags::CF_STATIC_OBJECT);
        mRigidBody->setActivationState(ISLAND_SLEEPING);
        break;
    }
}

BulletRigidBody::SimulationType BulletRigidBody::getSimulationType() const
{
    return mSimType;
}

void BulletRigidBody::updateConstructionInfo() {
    if (mConstructionInfo.m_collisionShape != nullptr)
    {
        // This rigid body was not loaded so its construction must be finished
        Collider *collider = (Collider *) owner_object_->getComponent(COMPONENT_TYPE_COLLIDER);
        if (collider)
        {
            bool isDynamic = (getMass() != 0.f);
            delete mConstructionInfo.m_collisionShape;
            mRigidBody->setMotionState(this);
            mRigidBody->setMassProps(mConstructionInfo.m_mass, mConstructionInfo.m_localInertia);
            mConstructionInfo.m_collisionShape = convertCollider2CollisionShape(collider);
            if (isDynamic)
            {
                mConstructionInfo.m_collisionShape->calculateLocalInertia(getMass(),
                        mConstructionInfo.m_localInertia);
            }
            mRigidBody->setCollisionShape(mConstructionInfo.m_collisionShape);
            mRigidBody->setMassProps(getMass(), mConstructionInfo.m_localInertia);
            mRigidBody->updateInertiaTensor();
            updateColisionShapeLocalScaling();
        }
        else
        {
            LOGE("PHYSICS: Cannot attach rigid body without collider");
        }
    }

    mRigidBody->setMotionState(this);
    getWorldTransform(prevPos);
}

void BulletRigidBody::finalize() {

    if (mRigidBody->getCollisionShape()) {
        mConstructionInfo.m_collisionShape = 0;
        delete mRigidBody->getCollisionShape();
    }

    if (mRigidBody) {
        delete mRigidBody;
        mRigidBody = 0;
    }
}

void BulletRigidBody::getRotation(float &w, float &x, float &y, float &z) {
    btTransform trans;

    if (mRigidBody->getMotionState()) {
        mRigidBody->getMotionState()->getWorldTransform(trans);
    } else {
        trans = mRigidBody->getCenterOfMassTransform();
    }

    btQuaternion rotation = trans.getRotation();

    w = rotation.getW();
    z = rotation.getZ();
    y = rotation.getY();
    x = rotation.getX();
}

void BulletRigidBody::getTranslation(float &x, float &y, float &z) {
    btTransform trans;
    if (mRigidBody->getMotionState()) {
        mRigidBody->getMotionState()->getWorldTransform(trans);
    } else {
        trans = mRigidBody->getCenterOfMassTransform();
    }

    btVector3 pos = trans.getOrigin();

    z = pos.getZ();
    y = pos.getY();
    x = pos.getX();
}

void BulletRigidBody::setCenterOfMass(const Transform *t) {
    mRigidBody->setCenterOfMassTransform(convertTransform2btTransform(t));
}

void BulletRigidBody::getWorldTransform(btTransform &centerOfMassWorldTrans) const {
    Transform* trans = owner_object()->transform();

    centerOfMassWorldTrans = convertTransform2btTransform(trans)
                             * m_centerOfMassOffset.inverse();
}

void BulletRigidBody::setWorldTransform(const btTransform &centerOfMassWorldTrans) {
    Transform* trans = owner_object()->transform();
    btTransform aux; getWorldTransform(aux);

    if(std::abs(aux.getOrigin().getX() - prevPos.getOrigin().getX()) >= 0.1f ||
       std::abs(aux.getOrigin().getY() - prevPos.getOrigin().getY()) >= 0.1f ||
       std::abs(aux.getOrigin().getZ() - prevPos.getOrigin().getZ()) >= 0.1f)
    {
        mRigidBody->setWorldTransform(aux);
        prevPos = aux;
        //TODO: incomplete solution
    }
    else
    {
        btTransform physicBody = (centerOfMassWorldTrans  * m_centerOfMassOffset);
        convertBtTransform2Transform(physicBody, trans);
        prevPos = physicBody;
    }
    //convertBtTransform2Transform(centerOfMassWorldTrans * m_centerOfMassOffset, trans);
}

void BulletRigidBody::applyCentralForce(float x, float y, float z) {
    mRigidBody->applyCentralForce(btVector3(x, y, z));
}

void BulletRigidBody::applyTorque(float x, float y, float z) {
    mRigidBody->applyTorque(btVector3(x, y, z));
}

float BulletRigidBody::center_x() const {
    return m_centerOfMassOffset.getOrigin().getX();
}

float BulletRigidBody::center_y() const {
    return m_centerOfMassOffset.getOrigin().getY();
}

float BulletRigidBody::center_z() const {
    return m_centerOfMassOffset.getOrigin().getZ();
}

void  BulletRigidBody::set_center(float x, float y, float z) {
    m_centerOfMassOffset.setOrigin(btVector3(x, y, z));
}

float BulletRigidBody::rotation_w() const {
    return m_centerOfMassOffset.getRotation().getW();
}

float BulletRigidBody::rotation_x() const {
    return m_centerOfMassOffset.getRotation().getX();
}

float BulletRigidBody::rotation_y() const {
    return m_centerOfMassOffset.getRotation().getY();
}

float BulletRigidBody::rotation_z() const {
    return m_centerOfMassOffset.getRotation().getZ();
}

void  BulletRigidBody::set_rotation(float w, float x, float y, float z) {
    m_centerOfMassOffset.setRotation(btQuaternion(x, y, z, w));
}

float BulletRigidBody::scale_x() const {
    return mScale.getX();
}

float BulletRigidBody::scale_y() const {
    return mScale.getY();
}

float BulletRigidBody::scale_z() const {
    return mScale.getZ();
}

void  BulletRigidBody::set_scale(float x, float y, float z) {
    mScale.setValue(x, y, z);

    //TODO: verify scaling upon graphic object update & diminish dependency
    updateColisionShapeLocalScaling();
}

void  BulletRigidBody::updateColisionShapeLocalScaling() {
    btVector3 ownerScale;
    SceneObject* owner = owner_object();
    if (owner) {
        Transform* trans = owner->transform();
        ownerScale.setValue(trans->scale_x(),
                            trans->scale_y(),
                            trans->scale_z());
    } else {
        ownerScale.setValue(1.0f, 1.0f, 1.0f);
    }

    mRigidBody->getCollisionShape()->setLocalScaling(mScale * ownerScale);
}


void BulletRigidBody::setGravity(float x, float y, float z) {
    mRigidBody->setGravity(btVector3(x, y, z));
}

void BulletRigidBody::setDamping(float linear, float angular) {
    mRigidBody->setDamping(linear, angular);
}

void BulletRigidBody::setLinearVelocity(float x, float y, float z) {
    mRigidBody->setLinearVelocity(btVector3(x, y, z));
}

void BulletRigidBody::setAngularVelocity(float x, float y, float z) {
    mRigidBody->setAngularVelocity(btVector3(x, y, z));
}

void BulletRigidBody::setAngularFactor(float x, float y, float z) {
    mRigidBody->setAngularFactor(btVector3(x, y, z));
}

void BulletRigidBody::setLinearFactor(float x, float y, float z) {
    mRigidBody->setLinearFactor(btVector3(x, y, z));
}

void BulletRigidBody::setFriction(float n) {
    mRigidBody->setFriction(n);
}

void BulletRigidBody::setRestitution(float n) {
    mRigidBody->setRestitution(n);
}

void BulletRigidBody::setSleepingThresholds(float linear, float angular) {
    mRigidBody->setSleepingThresholds(linear, angular);
}

void BulletRigidBody::setCcdMotionThreshold(float n) {
    mRigidBody->setCcdMotionThreshold(n);
}

void BulletRigidBody::setCcdSweptSphereRadius(float n) {
    mRigidBody->setCcdSweptSphereRadius(n);
}

void BulletRigidBody::setContactProcessingThreshold(float n) {
    mRigidBody->setContactProcessingThreshold(n);
}

void BulletRigidBody::setIgnoreCollisionCheck(PhysicsRigidBody *collisionObj, bool ignore) {
    mRigidBody->setIgnoreCollisionCheck(((BulletRigidBody *) collisionObj)->getRigidBody(),
                                        ignore);
}

void BulletRigidBody::getGravity(float *v3) const {
    btVector3 result = mRigidBody->getLinearFactor();
    v3[0] = result.getX();
    v3[1] = result.getY();
    v3[2] = result.getZ();
}

void BulletRigidBody::getDamping(float &angular, float &linear) const {
    linear = mRigidBody->getLinearDamping();
    angular = mRigidBody->getAngularDamping();
}

void BulletRigidBody::getLinearVelocity(float *v3) const {
    btVector3 result = mRigidBody->getLinearVelocity();
    v3[0] = result.getX();
    v3[1] = result.getY();
    v3[2] = result.getZ();
}

void BulletRigidBody::getAngularVelocity(float *v3) const {
    btVector3 result = mRigidBody->getAngularVelocity();
    v3[0] = result.getX();
    v3[1] = result.getY();
    v3[2] = result.getZ();
}

void BulletRigidBody::getAngularFactor(float *v3) const {
    btVector3 result = mRigidBody->getAngularFactor();
    v3[0] = result.getX();
    v3[1] = result.getY();
    v3[2] = result.getZ();
}

void BulletRigidBody::getLinearFactor(float *v3) const {
    btVector3 result = mRigidBody->getLinearFactor();
    v3[0] = result.getX();
    v3[1] = result.getY();
    v3[2] = result.getZ();
}

const float  BulletRigidBody::getFriction() const {
    return mRigidBody->getFriction();
}

const float  BulletRigidBody::getRestitution() const {
    return mRigidBody->getRestitution();
}

const float  BulletRigidBody::getCcdMotionThreshold() const {
    return mRigidBody->getCcdMotionThreshold();
}

const float  BulletRigidBody::getCcdSweptSphereRadius() const {
    return mRigidBody->getCcdSweptSphereRadius();
}

const float  BulletRigidBody::getContactProcessingThreshold() const {
    return mRigidBody->getContactProcessingThreshold();
}

}
