#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <dlfcn.h>
#include "pojavexec.h"
#include "driver_helper/nsbypass.h"

static char* native_directory = NULL;
static pojavexec_renderspec_t renderspec = {0};

const char* pojavexec_getNativeDirectory() {
    return native_directory;
}

const pojavexec_renderspec_t* pojavexec_getRenderSpec() {
    return &renderspec;
}

static void* egl_acquire_normal(const char* path) {
    return dlopen(path, RTLD_GLOBAL | RTLD_NOW);
}

static void* egl_acquire_bypass(const char* path) {
    if (native_directory != NULL) {
        linker_ns_load(native_directory);
    }
    return linker_ns_dlopen(path, RTLD_GLOBAL | RTLD_NOW);
}

JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_utils_JREUtils_nsetRendererLibraryPath(JNIEnv *env, jclass clazz, jstring path) {
    if (native_directory != NULL) free(native_directory);
    const char* path_c = (*env)->GetStringUTFChars(env, path, NULL);
    native_directory = strdup(path_c);
    (*env)->ReleaseStringUTFChars(env, path, path_c);
}

JNIEXPORT jboolean JNICALL
Java_net_kdt_pojavlaunch_utils_JREUtils_configureRenderspec(JNIEnv *env, jclass clazz, jstring eglPath, jboolean useLoaderBypass, jboolean useGles, jint glesVersion) {
    if (renderspec.egl_path != NULL) free((void*)renderspec.egl_path);

    const char* path_c = (*env)->GetStringUTFChars(env, eglPath, NULL);
    renderspec.egl_path = strdup(path_c);
    (*env)->ReleaseStringUTFChars(env, eglPath, path_c);

    if (useLoaderBypass) {
        renderspec.egl_acquire = egl_acquire_bypass;
    } else {
        renderspec.egl_acquire = egl_acquire_normal;
    }

    renderspec.force_gles_context = useGles;
    renderspec.override_major_version = glesVersion;

    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_utils_JREUtils_configureRenderspecDisplay(JNIEnv *env, jclass clazz, jint width, jint height, jint refreshRate) {
    renderspec.disp_width = width;
    renderspec.disp_height = height;
    renderspec.disp_hz = refreshRate;
}
