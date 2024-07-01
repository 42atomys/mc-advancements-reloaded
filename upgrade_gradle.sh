#!/bin/bash

export GRADLE_VERSION=$1
export SDKMAN_DIR="/usr/local/sdkman"

if [ ! -f /usr/local/sdkman/bin/sdkman-init.sh ]; then
  echo "> sdkman not found, installing..."
  curl -s "https://get.sdkman.io" | bash
fi

source "$SDKMAN_DIR/bin/sdkman-init.sh"
sed -i 's/sdkman_auto_answer=.*/sdkman_auto_answer=true/' /usr/local/sdkman/etc/config
sdk install gradle $GRADLE_VERSION

sed -i "s/distributions\/gradle-.*-all.zip/distributions\/gradle-$GRADLE_VERSION-all.zip/" ./gradle/wrapper/gradle-wrapper.properties
gradle wrapper --gradle-version $GRADLE_VERSION --distribution-type all
