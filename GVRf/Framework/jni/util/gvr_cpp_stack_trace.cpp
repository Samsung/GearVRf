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

#include "util/gvr_log.h"
#include "gvr_cpp_stack_trace.h"

#include <iostream>
#include <sstream>
#include <iomanip>

#include <unwind.h>
#include <dlfcn.h>

namespace {

    struct BacktraceState
    {
        void** current;
        void** end;
    };

    static _Unwind_Reason_Code unwindCallback(struct _Unwind_Context* context, void* arg)
    {
        BacktraceState* state = static_cast<BacktraceState*>(arg);
        uintptr_t pc = _Unwind_GetIP(context);
        if (pc) {
            if (state->current == state->end) {
                return _URC_END_OF_STACK;
            } else {
                *state->current++ = reinterpret_cast<void*>(pc);
            }
        }
        return _URC_NO_REASON;
    }

    size_t captureBacktrace(void** buffer, size_t max)
    {
        BacktraceState state = {buffer, buffer + max};
        _Unwind_Backtrace(unwindCallback, &state);

        return state.current - buffer;
    }

    void dumpBacktrace(std::ostream& os, void** addrs, size_t count)
    {
        for (size_t idx = 0; idx < count; ++idx) {
            const void* addr = addrs[idx];
            const char* symbol = "";

            Dl_info info;
            if (dladdr(addr, &info) && info.dli_sname) {
                symbol = info.dli_sname;
            }

            os << "  #" << std::setw(2) << idx << ": " << addr << "  " << symbol << "\n";
        }
    }

} // namespace

void printStackTrace(unsigned int max_frames)
{
    void* buffer[max_frames];
    std::ostringstream oss;

    dumpBacktrace(oss, buffer, captureBacktrace(buffer, max_frames));

    LOGD("%s", oss.str().c_str());
}
