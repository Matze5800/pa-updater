#
# Copyright (C) 2013 PA Updater (Simon Matzeder and Parthipan Ramesh)
#
# Licensed under the GNU GPLv2 license
#
# The text of the license can be found in the LICENSE file
# or at https://www.gnu.org/licenses/gpl-2.0.txt
#

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES += $(call all-java-files-under, src)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v13

LOCAL_PACKAGE_NAME := PA-Updater

include $(BUILD_PACKAGE)