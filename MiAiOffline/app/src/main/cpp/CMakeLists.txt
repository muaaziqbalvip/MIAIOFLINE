cmake_minimum_required(VERSION 3.22.1)
project(miai_inference)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ---------------------------------------------------------------------------
# llama.cpp integration point.
#
# This skeleton does NOT vendor llama.cpp source yet (it's a large external
# project). To complete native inference, do ONE of:
#
#   Option A (recommended) - git submodule:
#     git submodule add https://github.com/ggerganov/llama.cpp app/src/main/cpp/llama.cpp
#     then uncomment the add_subdirectory line below.
#
#   Option B - download a prebuilt llama.cpp Android AAR and link against it
#     instead of building from source here.
#
# Once llama.cpp is present, jni_bridge.cpp's calls into llama_* functions
# will compile and the app will perform real on-device inference.
# ---------------------------------------------------------------------------

# add_subdirectory(llama.cpp)

add_library(miai_inference SHARED
    jni_bridge.cpp
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(miai_inference
    ${log-lib}
    ${android-lib}
    # llama   <-- uncomment once llama.cpp submodule is added (Option A above)
)

target_include_directories(miai_inference PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    # ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp  <-- uncomment once submodule added
)
