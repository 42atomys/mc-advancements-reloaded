#!/bin/bash

YARN_VERSION=$(grep -i 'yarn_mappings' gradle.properties | cut -d'=' -f2)
MOD_VERSION=$(grep -i 'mod_version' gradle.properties | cut -d'=' -f2)
echo "Updating plugin to $MOD_VERSION"

./gradlew migrateMappings --mappings $YARN_VERSION
echo "Mappings updated to $YARN_VERSION"

sed -i 's/"version": ".*"/"version": "'$MOD_VERSION'"/' ./src/main/resources/fabric.mod.json
echo "Updated fabric.mod.json to $MOD_VERSION"
