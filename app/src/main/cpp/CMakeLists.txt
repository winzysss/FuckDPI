cmake_minimum_required(VERSION 3.22.1)

project(fuckdpi_native)

file(GLOB DESYNC_SRC core/*.c)
list(REMOVE_ITEM DESYNC_SRC ${CMAKE_CURRENT_SOURCE_DIR}/core/win_service.c)

add_library(desync SHARED ${DESYNC_SRC} desync-jni.c tunnel-jni.c)
target_include_directories(desync PRIVATE core)

target_compile_options(desync PRIVATE -std=c99 -O2 -Wall -Wno-unused -Wextra -Wno-unused-parameter -pedantic)
target_compile_definitions(desync PRIVATE ANDROID_APP)

target_link_libraries(desync PRIVATE android log dl)
target_link_options(desync PRIVATE "-Wl,--wrap=daemon")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
