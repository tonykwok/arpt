# arpt
Android Resource Pruning Tool

## usage

Android.mk

```
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := <YOUR PACKAGE NAME>

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

ifeq (true, $(call is-greater-than, $(PLATFORM_SDK_VERSION), 30))
LOCAL_PRODUCT_MODULE := true
else
LOCAL_VENDOR_MODULE := true
endif

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, app/src/main/res)
LOCAL_FULL_MANIFEST_FILE := $(addprefix $(LOCAL_PATH)/, app/src/main/AndroidManifest.xml)

RESOURCE_PRUNING_TARGET_PRODUCT_NAME := <YOUR PRODUCT NAME>
RESOURCE_PRUNING_ENABLED := <true/false>

ifeq ($(RESOURCE_PRUNING_ENABLED),true)
include $(LOCAL_PATH)/arpt.mk
endif

$(info $(LOCAL_PACKAGE_NAME): LOCAL_RESOURCE_DIR: $(LOCAL_RESOURCE_DIR))
$(info $(LOCAL_PACKAGE_NAME): LOCAL_FULL_MANIFEST_FILE: $(LOCAL_FULL_MANIFEST_FILE))

include $(BUILD_RRO_PACKAGE)
```

arpt.mk

```
ifeq ($(LOCAL_FULL_MANIFEST_FILE),)
$(error $(LOCAL_PACKAGE_NAME): LOCAL_FULL_MANIFEST_FILE not defined)
endif

ifeq ($(LOCAL_RESOURCE_DIR),)
$(error $(LOCAL_PACKAGE_NAME): LOCAL_RESOURCE_DIR not defined)
endif

ifneq ($(words $(LOCAL_RESOURCE_DIR)),1)
$(error $(LOCAL_PACKAGE_NAME): multiple LOCAL_RESOURCE_DIR not supported: $(LOCAL_RESOURCE_DIR))
endif

arpt_exec_file := $(LOCAL_PATH)/arpt.jar
arpt_rule_file := $(LOCAL_PATH)/arpt.xml

arpt_product_name := $(CAMERA_PRODUCT)

# DIR: out/target/common/obj/APPS/MiuiCameraOverlay_intermediates/arpt
arpt_intermediates_dir := $(call intermediates-dir-for,APPS,$(LOCAL_PACKAGE_NAME),,COMMON)/arpt

arpt_source_resource_dir := $(LOCAL_RESOURCE_DIR)
arpt_target_resource_dir := $(arpt_intermediates_dir)/res

arpt_source_manifest_file := $(LOCAL_FULL_MANIFEST_FILE)
arpt_target_manifest_file := $(arpt_intermediates_dir)/AndroidManifest.xml


.PHONY: force_resource_pruning

force_resource_pruning :
	@echo "Start resource pruning..."
	mkdir -p $(arpt_target_resource_dir) && rm -rf $(arpt_target_resource_dir)
	cp -R $(arpt_source_resource_dir) $(arpt_target_resource_dir)
	java -jar $(arpt_exec_file) \
		 -x $(arpt_rule_file) \
		 -n $(arpt_product_name) \
		 $(arpt_target_resource_dir)


# Read 'Build System Changes for Android.mk Writers' for more details
# https://android.googlesource.com/platform/build/+/master/Changes.md
$(arpt_target_manifest_file) : $(arpt_source_manifest_file) | force_resource_pruning
	@echo "Build target: $@"
	mkdir -p $(dir $@) && rm -f $@
	cat $(arpt_source_manifest_file) > $@


LOCAL_RESOURCE_DIR := $(arpt_target_resource_dir)

# WHERE THE MAGIC HAPPENS
LOCAL_FULL_MANIFEST_FILE := $(arpt_target_manifest_file)
```

arpt.xml

```
<?xml version="1.0" encoding="utf-8" ?>
<resources>
    <string availability="smith">
        <item>camera_object_tracking_setting_title</item>
        <item>camera_object_tracking_setting_description</item>
    </string>

    <string availability="smith|lisa">
        <item>camera_video_recording_device_too_hot_tips</item>
    </string>

    <string-array availability="smith">
        <item>camera_video_quality_values</item>
    </string-array>

    <plurals availability="lisa">
        <item>camera_burst_capture_count</item>
    </plurals>

    <file availability="smith">
        <item>drawable/camera_asd_scene_car.png</item>
    </file>
</resources>
```
