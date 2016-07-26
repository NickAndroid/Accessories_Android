LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := tests

# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := imageloadertest

LOCAL_JACK_ENABLED := disabled

# Matching ../Android.mk
LOCAL_SDK_VERSION := current

LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := \
    android.opt.imageloader

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))


