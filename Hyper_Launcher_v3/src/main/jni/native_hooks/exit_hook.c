//
// Created by maks on 15.01.2025.
//
#include "native_hooks.h"

#include <jni.h>
#include <stdbool.h>
#include <bytehook.h>
#include <dlfcn.h>
#include <stdlib.h>
#include "stdio_is.h"

#define TAG __FILE_NAME__
#include <log.h>
#include <string.h>

#include <android/dlext.h>
#include "../driver_helper/nsbypass.h"

static void* custom_dlopen(const char* filename, int flags) {
    if (filename != NULL && strcmp(filename, "libpthread.so.0") == 0) {
        return BYTEHOOK_CALL_PREV(custom_dlopen, void* (*)(const char*, int), "libc.so", flags);
    }
    return BYTEHOOK_CALL_PREV(custom_dlopen, void* (*)(const char*, int), filename, flags);
}

static void* custom_android_dlopen_ext(const char* filename, int flags, const android_dlextinfo* info) {
    if (filename != NULL && strcmp(filename, "libpthread.so.0") == 0) {
        return BYTEHOOK_CALL_PREV(custom_android_dlopen_ext, void* (*)(const char*, int, const android_dlextinfo*), "libc.so", flags, info);
    }
    return BYTEHOOK_CALL_PREV(custom_android_dlopen_ext, void* (*)(const char*, int, const android_dlextinfo*), filename, flags, info);
}

static void create_hooks(bytehook_hook_all_t bytehook_hook_all_p) {
    if (bytehook_hook_all_p != NULL) {
        bytehook_hook_all_p(NULL, "dlopen", &custom_dlopen, NULL, NULL);
        bytehook_hook_all_p(NULL, "android_dlopen_ext", &custom_android_dlopen_ext, NULL, NULL);
    }

    // Alias libc.so to libpthread.so.0 to satisfy mod dependencies
    const char* tmpdir = getenv("TMPDIR");
    if (tmpdir != NULL) {
        void* pthread_handle = linker_dlopen_unique_global(tmpdir, "libc.so", "libpthread.so.0", RTLD_GLOBAL | RTLD_NOW);
        if (pthread_handle != NULL) {
            LOGI("Successfully aliased libc.so to libpthread.so.0 in global namespace.");
        } else {
            LOGW("Failed to alias libc.so to libpthread.so.0: %s", dlerror());
        }
    }

    // Only apply chmod hooks on devices where the game directory is in games/PojavLauncher
    // which is below API 29
    if(android_get_device_api_level() < 29) {
        create_chmod_hooks(bytehook_hook_all_p);
    }
}

static bool init_hooks() {
    void* bytehook_handle = dlopen("libbytehook.so", RTLD_NOW);
    if(bytehook_handle == NULL) {
        goto dlerror;
    }

    bytehook_hook_all_t bytehook_hook_all_p;
    int (*bytehook_init_p)(int mode, bool debug);

    bytehook_hook_all_p = dlsym(bytehook_handle, "bytehook_hook_all");
    bytehook_init_p = dlsym(bytehook_handle, "bytehook_init");

    if(bytehook_hook_all_p == NULL || bytehook_init_p == NULL) {
        goto dlerror;
    }
    int bhook_status = bytehook_init_p(BYTEHOOK_MODE_AUTOMATIC, false);
    if(bhook_status == BYTEHOOK_STATUS_CODE_OK) {
        create_hooks(bytehook_hook_all_p);
        return true;
    } else {
        LOGE("bytehook_init failed (%i)", bhook_status);
        dlclose(bytehook_handle);
        return false;
    }

    dlerror:
    if(bytehook_handle != NULL) dlclose(bytehook_handle);
    LOGE("Failed to load hook library: %s", dlerror());
    return false;
}

JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_utils_JREUtils_initializeHooks(JNIEnv *env, jclass clazz) {
    LOGI("Initializing native hooks...");
    bool hooks_ready = init_hooks();
    if(!hooks_ready) {
        LOGE("Failed to initialize native hooks!");
    } else {
        LOGI("Native hooks initialized successfully.");
    }
}
