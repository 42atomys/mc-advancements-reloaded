#!/bin/bash

# Install xmllint if not installed
if ! command -v xmllint &> /dev/null; then
  echo "> xmllint not found, installing..."
  sudo apt-get update -y
  sudo apt-get -y install libxml2-utils
fi

# Install envsubst if not installed
if ! command -v envsubst &> /dev/null; then
  echo "> envsubst not found, installing..."
  sudo apt-get update -y
  sudo apt-get -y install gettext-base
fi

export OWO_CONFIG_VERSION="0.12.10+1.21"

echo "> Fetching latest versions of the game, loader, yarn mappings, fabric api, and loom plugin..."
# Get the latest version of the game
export GAME_VERSION=$(curl -s https://meta.fabricmc.net/v2/versions/game | jq -r '[.[] | select(.stable == true)] | sort_by(.version) | .[-1].version')
# Get the release version (1.20 for 1.20.2)
export GAME_RELEASE=$(echo $GAME_VERSION | cut -d '.' -f 1,2)
# Get the latest version of the loader
export LOADER_VERSION=$(curl -s https://meta.fabricmc.net/v2/versions/loader | jq -r '[.[] | select(.stable == true)] | .[0].version')
# Get the version of the yarn mappings for the game version
export YARN_VERSION=$(curl -s https://meta.fabricmc.net/v2/versions/yarn | jq -r "[.[] | select(.gameVersion == \"$GAME_VERSION\")] | sort_by(.build) | .[-1].version")
# Get the version of the fabric api for the game version
export FABRIC_API_VERSION=$(curl -s "https://api.modrinth.com/v2/project/fabric-api/version?game_versions=%5B%22$GAME_VERSION%22%5D" | jq -r 'sort_by(.date_published) | .[-1].version_number')
# Get the version of the fabric loom plugin
export LOOM_VERSION=$(curl -s "https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml" | xmllint --xpath "//metadata/versioning/versions/version[contains(., '-SNAPSHOT')]" - | grep -oP '>[0-9\.-]+-SNAPSHOT<' | tail -1 | sed 's/[><]//g')

# Update the gradle.properties file
echo "> Updating gradle.properties with the latest versions..."
envsubst < gradle.properties.template > gradle.properties

if [[ "$CI" = "true" ]]; then
  echo "> Running in CI export env to $GITHUB_OUTPUT"

  echo "game_version=$GAME_VERSION" >> "$GITHUB_OUTPUT"
  echo "game_release=$GAME_RELEASE" >> "$GITHUB_OUTPUT"
  echo "loader_version=$LOADER_VERSION" >> "$GITHUB_OUTPUT"
  echo "yarn_version=$YARN_VERSION" >> "$GITHUB_OUTPUT"
  echo "fabric_api_version=$FABRIC_API_VERSION" >> "$GITHUB_OUTPUT"
  echo "loom_version=$LOOM_VERSION" >> "$GITHUB_OUTPUT"
else
  echo "> Local run, printing the versions..."
fi

echo " | game_version=$GAME_VERSION"
echo " | game_release=$GAME_RELEASE"
echo " | loader_version=$LOADER_VERSION"
echo " | yarn_version=$YARN_VERSION"
echo " | fabric_api_version=$FABRIC_API_VERSION"
echo " | loom_version=$LOOM_VERSION"

echo "> Static versions:"
echo " | owo_config_version=$OWO_CONFIG_VERSION"

echo "> Done!"
