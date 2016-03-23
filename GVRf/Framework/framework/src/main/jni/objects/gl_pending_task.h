#ifndef GL_PENDING_TASK_H_
#define GL_PENDING_TASK_H_

class GLPendingTask {
    virtual void runPendingGL() = 0;
};

#endif // GL_PENDING_TASK_H_
