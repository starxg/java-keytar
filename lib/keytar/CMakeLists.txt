cmake_minimum_required(VERSION 2.8...3.17)

project(keytar)

add_definitions(-std=c++11)

include_directories(
    keytar-core/include
)

include_directories(
    include

    $ENV{JAVA_HOME}/include/

    if(APPLE)
        $ENV{JAVA_HOME}/include/darwin/
    elseif(WIN32)
        $ENV{JAVA_HOME}/include/win32/
    elseif(UNIX)
        $ENV{JAVA_HOME}/include/linux/
    endif()
)


if(APPLE)
    set(keytar-core keytar-core/keytar_mac.cc)
elseif(WIN32)
    set(keytar-core keytar-core/keytar_win.cc)
elseif(UNIX)

    find_package(PkgConfig REQUIRED)
    pkg_search_module(GLIB REQUIRED glib-2.0)
    pkg_search_module(LIBSECRET REQUIRED libsecret-1)
    link_libraries(${LIBSECRET_LIBRARIES})

    include_directories(
        ${GLIB_INCLUDE_DIRS}
        ${LIBSECRET_INCLUDE_DIRS}
    )

    set(keytar-core keytar-core/keytar_posix.cc)
endif()

add_library(keytar SHARED ${PROJECT_SOURCE_DIR}/src/keytar.cc ${keytar-core} )

if(APPLE)
    target_link_libraries(keytar "-framework Security" "-framework CoreFoundation")
endif()
