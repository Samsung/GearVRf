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

#include <android/asset_manager.h>

#include <btBulletCollisionCommon.h>
#include <btBulletDynamicsCommon.h>
#include <Serialize/BulletWorldImporter/btBulletWorldImporter.h>
#include <BulletDynamics/ConstraintSolver/btConstraintSolver.h>
#include <BulletDynamics/ConstraintSolver/btConeTwistConstraint.h>
#include <BulletDynamics/ConstraintSolver/btFixedConstraint.h>
#include <BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h>
#include <BulletDynamics/ConstraintSolver/btHingeConstraint.h>
#include <BulletDynamics/ConstraintSolver/btPoint2PointConstraint.h>
#include <BulletDynamics/ConstraintSolver/btSliderConstraint.h>
#include <BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h>
#include <Serialize/BulletFileLoader/btBulletFile.h>
#include <BulletDynamics/Dynamics/btDynamicsWorld.h>

#include "bullet_fileloader.h"
#include "bullet_world.h"
#include "bullet_rigidbody.h"
#include "bullet_conetwistconstraint.h"
#include "bullet_fixedconstraint.h"
#include "bullet_generic6dofconstraint.h"
#include "bullet_hingeconstraint.h"
#include "bullet_point2pointconstraint.h"
#include "bullet_sliderconstraint.h"

static btMatrix3x3 matrixInvIdty(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f);
static btTransform transformInvIdty(matrixInvIdty);

namespace gvr {

static void createBulletRigidBodies(btBulletWorldImporter *importer)
{
    for (int i = 0; i < importer->getNumRigidBodies(); i++)
    {
        btRigidBody *rb = reinterpret_cast<btRigidBody*>(importer->getRigidBodyByIndex(i));

        if (nullptr == importer->getNameForPointer(rb))
        {
            // A rigid body has no name.
            continue;
        }

        // btRigidBody userPointer will point to the newly created BulletRigidBody
        BulletRigidBody *brb = new BulletRigidBody(rb);
    }
}

static void createBulletP2pConstraint(btPoint2PointConstraint *p2p, bool needRotate)
{
    // Constraint userPointer will point to newly created BulletPoint2PointConstraint
    BulletPoint2PointConstraint *bp2p = new BulletPoint2PointConstraint(p2p);

    if (needRotate)
    {
        // Adapting pivot to GVRf coordinates system
        btVector3 pivot = p2p->getPivotInA();
        float t = pivot.getZ();
        pivot.setZ(-pivot.getY());
        pivot.setY(t);
        p2p->setPivotA(pivot);

        pivot = p2p->getPivotInB();
        t = pivot.getZ();
        pivot.setZ(-pivot.getY());
        pivot.setY(t);
        p2p->setPivotB(pivot);
    }
}

static void createBulletHingeConstraint(btHingeConstraint *hg, bool needRotate)
{
    BulletHingeConstraint *bhg = new BulletHingeConstraint(hg);

    if (needRotate)
    {
        btTransform t = hg->getAFrame();
        hg->getAFrame().mult(transformInvIdty, t);

        t = hg->getBFrame();
        hg->getBFrame().mult(transformInvIdty, t);
    }
}

static void createBulletConeTwistConstraint(btConeTwistConstraint *ct, bool needRotate)
{
    BulletConeTwistConstraint *bct = new BulletConeTwistConstraint(ct);

    if (needRotate)
    {
        btTransform tA = ct->getAFrame();
        btTransform tB = ct->getBFrame();

        btTransform t = tA;
        tA.mult(transformInvIdty, t);

        t = tB;
        tB.mult(transformInvIdty, t);

        ct->setFrames(tA, tB);
    }
}

static void createBulletGenericConstraint(btGeneric6DofConstraint *gen, bool needRotate)
{
    BulletGeneric6dofConstraint *bct = new BulletGeneric6dofConstraint(gen);

    if (needRotate)
    {
        btTransform tA = gen->getFrameOffsetA();
        btTransform tB = gen->getFrameOffsetB();

        btTransform t = tA;
        tA.mult(transformInvIdty, t);

        t = tB;
        tB.mult(transformInvIdty, t);

        gen->setFrames(tA, tB);
    }
}

static void createBulletFixedConstraint(btFixedConstraint *fix, bool needRotate)
{
    BulletFixedConstraint *bfix = new BulletFixedConstraint(fix);

    if (needRotate)
    {
        btTransform tA = fix->getFrameOffsetA();
        btTransform tB = fix->getFrameOffsetB();

        btTransform t = tA;
        tA.mult(transformInvIdty, t);

        t = tB;
        tB.mult(transformInvIdty, t);

        fix->setFrames(tA, tB);
    }
}

static void createBulletSliderConstraint(btSliderConstraint *sld)
{
    BulletSliderConstraint *bsld = new BulletSliderConstraint(sld);
}

static void createBulletConstraints(btBulletWorldImporter *importer, bool needRotate)
{
    for (int i = 0; i < importer->getNumConstraints(); i++)
    {
        btTypedConstraint *constraint = importer->getConstraintByIndex(i);

        btRigidBody const *rbA = &constraint->getRigidBodyA();
        btRigidBody const *rbB = &constraint->getRigidBodyB();

        if (rbA->getUserPointer() == nullptr || rbB->getUserPointer() == nullptr)
        {
            // This constraint has at least one invalid rigid body and then it must to be ignored
            continue;
        }

        if (constraint->getConstraintType() == btTypedConstraintType::POINT2POINT_CONSTRAINT_TYPE)
        {
            createBulletP2pConstraint(static_cast<btPoint2PointConstraint*>(constraint), needRotate);
        }
        else if (constraint->getConstraintType() == btTypedConstraintType::HINGE_CONSTRAINT_TYPE)
        {
            createBulletHingeConstraint(static_cast<btHingeConstraint*>(constraint), needRotate);
        }
        else if (constraint->getConstraintType() == btTypedConstraintType::CONETWIST_CONSTRAINT_TYPE)
        {
            createBulletConeTwistConstraint(static_cast<btConeTwistConstraint*>(constraint), needRotate);
        }
        else if (constraint->getConstraintType() == btTypedConstraintType::D6_CONSTRAINT_TYPE ||
                 constraint->getConstraintType() == btTypedConstraintType::D6_SPRING_CONSTRAINT_TYPE)
        {
            // Blender exports generic constraint as generic spring constraint
            createBulletGenericConstraint(static_cast<btGeneric6DofConstraint*>(constraint), needRotate);
        }
        else if (constraint->getConstraintType() == btTypedConstraintType::FIXED_CONSTRAINT_TYPE ||
                 constraint->getConstraintType() == btTypedConstraintType::D6_SPRING_2_CONSTRAINT_TYPE)
        {
            // btFixedConstraint constraint is derived from btGeneric6DofSpring2Constraint and its
            // type is set to D6_SPRING_2_CONSTRAINT_TYPE instead of FIXED_CONSTRAINT_TYPE in
            // Bullet (at least up to) 2.87
            createBulletFixedConstraint(static_cast<btFixedConstraint*>(constraint), needRotate);
        }
        else if (constraint->getConstraintType() == btTypedConstraintType::SLIDER_CONSTRAINT_TYPE)
        {
            createBulletSliderConstraint(static_cast<btSliderConstraint*>(constraint));
        }
    }
}

BulletFileLoader::BulletFileLoader(char *buffer, size_t length, bool ignoreUpAxis) :
    PhysicsLoader(buffer, length, ignoreUpAxis), mCurrRigidBody(0), mCurrConstraint(0)
{
    bParse::btBulletFile *bullet_file = new bParse::btBulletFile(buffer, length);

    mImporter = new btBulletWorldImporter(nullptr);
    mImporter->loadFileFromMemory(bullet_file);

    bool needRotate;
    if (ignoreUpAxis)
    {
        needRotate = false;
    }
    else if (bullet_file->getFlags() & bParse::FD_DOUBLE_PRECISION)
    {
        btDynamicsWorldDoubleData* ddata =
                reinterpret_cast<btDynamicsWorldDoubleData*>(bullet_file->m_dynamicsWorldInfo[0]);
        double *gravity = reinterpret_cast<double *>(&ddata->m_gravity);
        needRotate = gravity[2] != 0.0;
    }
    else
    {
        btDynamicsWorldFloatData* fdata =
                reinterpret_cast<btDynamicsWorldFloatData*>(bullet_file->m_dynamicsWorldInfo[0]);
        float *gravity = reinterpret_cast<float*>(&fdata->m_gravity);
        needRotate = gravity[2] != 0.f;
    }

    delete bullet_file;

    createBulletRigidBodies(mImporter);

    createBulletConstraints(mImporter, needRotate);
}

BulletFileLoader::~BulletFileLoader()
{
    int i;

    for (i = 0; i < mImporter->getNumConstraints(); i++)
    {
        btTypedConstraint *constraint = mImporter->getConstraintByIndex(i);
        PhysicsConstraint *phcons = static_cast<PhysicsConstraint*>(constraint->getUserConstraintPtr());
        if (nullptr != phcons && ((PhysicsConstraint*)-1) != phcons)
        {
            // Constraint is valid, but was it attached?
            if (nullptr != phcons->owner_object())
            {
                continue;
            }
            else
            {
                delete phcons;
            }
        }
        else
        {
            delete constraint;
        }
    }

    for (i = 0; i < mImporter->getNumRigidBodies(); i++) {
        btRigidBody *rb = static_cast<btRigidBody*>(mImporter->getRigidBodyByIndex(i));
        BulletRigidBody *brb = static_cast<BulletRigidBody*>(rb->getUserPointer());
        if (nullptr != brb)
        {
            // Rigid body is valid, but was it attached?
            if (nullptr != brb->owner_object())
            {
                continue;
            }
            else
            {
                delete brb;
            }
        }
        else
        {
            delete rb;
        }
    }

    delete mImporter;
}

PhysicsRigidBody* BulletFileLoader::getNextRigidBody()
{
    PhysicsRigidBody *ret = nullptr;

    while (mCurrRigidBody < mImporter->getNumRigidBodies())
    {
        btRigidBody *rb = static_cast<btRigidBody*>(mImporter->getRigidBodyByIndex(mCurrRigidBody));
        ++mCurrRigidBody;

        if (nullptr != rb->getUserPointer())
        {
            ret = reinterpret_cast<BulletRigidBody*>(rb->getUserPointer());
            break;
        }
    }

    return ret;
}

const char* BulletFileLoader::getRigidBodyName(PhysicsRigidBody *body) const
{
    btRigidBody *rb = reinterpret_cast<BulletRigidBody*>(body)->getRigidBody();

    return mImporter->getNameForPointer(rb);
}

PhysicsConstraint* BulletFileLoader::getNextConstraint()
{
    PhysicsConstraint *ret = nullptr;

    while (mCurrConstraint < mImporter->getNumConstraints())
    {
        btTypedConstraint *constraint = mImporter->getConstraintByIndex(mCurrConstraint);
        ++mCurrConstraint;

        if (nullptr != constraint->getUserConstraintPtr() && ((void*)-1) != constraint->getUserConstraintPtr())
        {
            ret = static_cast<PhysicsConstraint *>(constraint->getUserConstraintPtr());
            break;
        }
    }

    return ret;
}

PhysicsRigidBody* BulletFileLoader::getConstraintBodyA(PhysicsConstraint *constraint)
{
    btTypedConstraint *btc = static_cast<btTypedConstraint*>(constraint->getUnderlying());
    btRigidBody *rbA = &btc->getRigidBodyA();
    return static_cast<PhysicsRigidBody*>(rbA->getUserPointer());
}

PhysicsRigidBody* BulletFileLoader::getConstraintBodyB(PhysicsConstraint *constraint)
{
    btTypedConstraint *btc = static_cast<btTypedConstraint*>(constraint->getUnderlying());
    btRigidBody *rbB = &btc->getRigidBodyB();
    return static_cast<PhysicsRigidBody*>(rbB->getUserPointer());
}

}
