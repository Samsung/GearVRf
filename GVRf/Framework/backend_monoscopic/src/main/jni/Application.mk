#
# Copyright 2015 Samsung Electronics Co., LTD
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
ifndef ARM64
APP_ABI := armeabi-v7a
else
APP_ABI := arm64-v8a
endif

APP_PLATFORM := android-19

ifndef GVRF_USE_CLANG
APP_STL := gnustl_static
NDK_TOOLCHAIN_VERSION := 4.9
else
APP_STL := c++_static
NDK_TOOLCHAIN_VERSION := clang
endif