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
#ifndef MATRIX_CALC_H
#define MATRIX_CALC_H


#include <string>
#include <contrib/glm/detail/type_mat.hpp>
#include <vector>

namespace gvr
{
    class MatrixCalc
    {
        enum NodeType
        {
            None = 0,
            Add = 2,
            Subtract = 3,
            Multiply = 4,
            Unary = 5,
            Invert = 5,
            Transpose = 6,
            Group = 7,
            InputOperand = 8,
            OutputOperand = 9
        };

        struct ExprNode
        {
            NodeType Type;
            int MatrixOffset;
            ExprNode* Operand[2];

            ExprNode (NodeType type = None) : Type(type)
            {
                Operand[0] = nullptr;
                Operand[1] = nullptr;
                MatrixOffset = -1;
            }

            ~ExprNode ()
            {
                if (Operand[0])
                {
                    delete Operand[0];
                }
                if (Operand[1])
                {
                    delete Operand[1];
                }
            }
        };

    public:
        MatrixCalc (const char* expression);

        bool calculate (const glm::mat4* inputMatrices, glm::mat4* outputMatrices);

        int getNumOutputs () const
        { return mExprTrees.size(); }

        std::string asString(ExprNode* node, int level = 0);

    protected:
        int compile (ExprNode** root, const char* expression);

        int parseOperand (ExprNode** root, const char* expr);

        bool eval (ExprNode* root, glm::mat4& result);

        static const char* mInputMatrixNames[20];
        std::vector<ExprNode*> mExprTrees;
        const glm::mat4* mInputMatrices;
        glm::mat4* mOutputMatrices;
    };
}


#endif // MATRIX_CALC_H
