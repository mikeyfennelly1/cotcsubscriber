#!/bin/bash

################################################
# SETUP
################################################
OS=$(uname)
if [[ "$OS" == "Darwin" ]]; then
	# OSX uses BSD readlink
	BASEDIR="$(dirname "$0")"
else
	BASEDIR=$(readlink -e "$(dirname "$0")/")
fi
pushd "${BASEDIR}/.."

set -eou pipefail

./gradlew build
cp ./app/build/libs/*SNAPSHOT.jar ./app.jar
VERSION=$(cat VERSION | tr -d '[:space:]')
IMAGE_NAME="mikeyfennelly/cotcsubscriber:${VERSION}"
docker build -t "${IMAGE_NAME}" -t "mikeyfennelly/cotcsubscriber:latest" .

docker login
docker push "${IMAGE_NAME}"
docker push "mikeyfennelly/cotcsubscriber:latest"

popd
