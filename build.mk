NDK_PATH=/home/luzi82/project/android/software/android-ndk-r3
ANDROIDKIT_PATH=/home/luzi82/project/android/tool/android_kit

.PHONY : all clean

all : .i18n_timestamp

.i18n_timestamp : i18n.ods
	${ANDROIDKIT_PATH}/ods2xml.sh mwp_strings_loc
	-mkdir res/values-zh-rMO/
	-mkdir res/values-zh-rTW/
	cp res/values-zh-rHK/mwp_strings_loc.xml res/values-zh-rMO
	cp res/values-zh-rHK/mwp_strings_loc.xml res/values-zh-rTW
	touch .i18n_timestamp

clean :
	-rm jni/version.h
