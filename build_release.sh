#!/bin/bash

# Copyright (C) 2017, 2018, 2019 Christopher Blay <chris.b.blay@gmail.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eu

ANDROID_SDK=/opt/android-sdk
BUILD_TOOLS_VERSION=29.0.2

if [ ! -f build_release.sh ]; then
    echo "Execute from containing directory"
    exit
fi

if [ $# -ne 1 ]; then
    echo "Usage: $0 keystore"
    exit
fi

if [ -f "$1" ]; then
    KEYSTORE="$1"
else
    echo "Specified keystore does not exist"
    exit
fi

for SDK in 24 29; do
    cd workspace
    bazel build -c opt :AndroidNougatEasterEggSdk${SDK}
    cp --no-preserve=mode \
        bazel-bin/AndroidNougatEasterEggSdk${SDK}_unsigned.apk ..
    cp --no-preserve=mode \
        bazel-bin/AndroidNougatEasterEggSdk${SDK}_proguard.map ..
    cd ..
    "$ANDROID_SDK/build-tools/$BUILD_TOOLS_VERSION/zipalign" -v -p 4 \
        AndroidNougatEasterEggSdk${SDK}_unsigned.apk \
        AndroidNougatEasterEggSdk${SDK}_aligned.apk
    rm AndroidNougatEasterEggSdk${SDK}_unsigned.apk
    "$ANDROID_SDK/build-tools/$BUILD_TOOLS_VERSION/apksigner" sign \
        --ks "$KEYSTORE" --out AndroidNougatEasterEggSdk${SDK}_signed.apk \
        AndroidNougatEasterEggSdk${SDK}_aligned.apk
    rm AndroidNougatEasterEggSdk${SDK}_aligned.apk
    mv AndroidNougatEasterEggSdk${SDK}_signed.apk \
        AndroidNougatEasterEggSdk${SDK}.apk
done
