#!/bin/bash

/usr/local/Caskroom/android-sdk/25.2.3/tools/bin/avdmanager create avd --force -n Nexus_4_API_25 -k  "system-images;android-25;google_apis;x86" -d "Nexus 4"

/usr/local/Caskroom/android-sdk/25.2.3/tools/emulator -avd Nexus_4_API_25 -skin 768x1280 & /usr/local/Caskroom/android-sdk/25.2.3/platform-tools/adb wait-for-device

WAIT_CMD="/usr/local/Caskroom/android-sdk/25.2.3/platform-tools/adb shell getprop init.svc.bootanim"

until $WAIT_CMD | grep -m 1 stopped; do
  echo "Waiting..."
  sleep 1
done