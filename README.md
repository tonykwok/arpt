# What's arpt?
ARPT is an **A**ndroid **R**esource **P**runing **T**ool that helps to remove resources that unrelated to the specific target products

# Get started

_Step1: Re-write your `Android.mk` file as following_

```makefile
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

ARPT_ENABLED := <true/false>
ifeq ($(ARPT_ENABLED),true)
include $(LOCAL_PATH)/arpt.mk
endif

$(info $(LOCAL_PACKAGE_NAME): LOCAL_RESOURCE_DIR: $(LOCAL_RESOURCE_DIR))
$(info $(LOCAL_PACKAGE_NAME): LOCAL_FULL_MANIFEST_FILE: $(LOCAL_FULL_MANIFEST_FILE))

include $(BUILD_RRO_PACKAGE)
```

_Step 2: Create another `Makefile` and name it to `arpt.mk`_

```makefile
ifneq (,$(LOCAL_RESOURCE_DIR))

$(info $(LOCAL_PACKAGE_NAME): start resource pruning for $(ARPT_TARGET_PRODUCT))

LOCAL_RESOURCE_DIR := $(foreach d,$(LOCAL_RESOURCE_DIR),$(call clean-path,$(d)))

ifneq ($(words $(LOCAL_RESOURCE_DIR)),1)
    $(error $(LOCAL_PACKAGE_NAME): multiple LOCAL_RESOURCE_DIR not supported: $(LOCAL_RESOURCE_DIR))
endif

arpt_exec_file := $(LOCAL_PATH)/arpt.jar
arpt_rule_file := $(LOCAL_PATH)/arpt.xml

arpt_target_product := <YOUR TARGET PRODUCT NAME>

# DIR: out/target/common/obj/APPS/<YOUR PACKAGE NAME>_intermediates/arpt
arpt_intermediates_dir := $(call intermediates-dir-for,APPS,$(LOCAL_PACKAGE_NAME),,COMMON)/arpt
arpt_log_file := $(addprefix $(arpt_intermediates_dir)/, arpt.log)

arpt_source_resource_dir := $(LOCAL_RESOURCE_DIR)
arpt_target_resource_dir := $(addprefix $(arpt_intermediates_dir)/, res)

# COPY resource dir to intermediates dir
$(shell mkdir -p $(arpt_target_resource_dir) && rm -rf $(arpt_target_resource_dir))
$(shell cp -R $(arpt_source_resource_dir) $(arpt_target_resource_dir))

# DO resource pruning
arpt_result := $(shell java -jar $(arpt_exec_file) \
                            -rule $(arpt_rule_file) \
                            -target $(arpt_target_product) \
                            $(arpt_target_resource_dir) > $(arpt_log_file); echo $$?)

# CHECK status
$(info $(file <$(arpt_log_file)))
ifneq ($(arpt_result),0)
    $(error $(LOCAL_PACKAGE_NAME): resource pruning failed!)
else
    $(info $(LOCAL_PACKAGE_NAME): resource pruning completed successfully!)
endif

LOCAL_RESOURCE_DIR := $(arpt_target_resource_dir)

endif
```

_Step 3: Define the pruning rule in `arpt.xml`_

```xml
<?xml version="1.0" encoding="utf-8" ?>
<resources>
    <string availability="smith">
        <item>camera_strings_object_tracking_title</item>
        <item>camera_strings_object_tracking_description</item>
    </string>

    <string availability="smith|lisa">
        <item>camera_strings_video_recording_device_too_hot</item>
    </string>

    <string-array availability="smith">
        <item>camera_settings_video_quality_values</item>
    </string-array>

    <plurals availability="lisa">
        <item>camera_strings_burst_capture_count</item>
    </plurals>

    <file availability="smith">
        <item>drawable/camera_icons_asd_scene_car</item>
    </file>
</resources>
```

_**NOTE:** In the sample above, all these 3 files are created in the root directory of the module_
