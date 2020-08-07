#!/bin/bash
set -ex

ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."

buildProject() {
    if [ "$BUILD" == "native" ]; then
        mvn -f "$ROOT/operator/" package -Pnative -DskipTests -Dnative-image.docker-build=true
    else
        mvn -f "$ROOT/operator/" package -DskipTests
    fi
}

buildImage() {
    mkdir -p "$ROOT/tmp"
    rm -f "$ROOT/tmp/$IMAGE_FILE_NAME"
    docker image rm -f "$TAG"
    docker build -f "$ROOT/operator/src/main/docker/Dockerfile.$BUILD" -t "$TAG" "$ROOT/operator/"
    docker image save -o "$ROOT/tmp/$IMAGE_FILE_NAME" $TAG
}

importImage() {
    "$ROOT/bin/k3d" load image "$ROOT/tmp/$IMAGE_FILE_NAME" -c "$CLUSTER"
}

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -c|--cluster)
    CLUSTER="$2"
    shift # past argument
    shift # past value
    ;;
    -b|--build)
    BUILD="$2"
    shift # past argument
    shift # past value
    ;;
    -t|--tag)
    TAG="$2"
    IMAGE_FILE_NAME="${TAG/\//-}.tar.gz"
    shift # past argument
    shift # past value
    ;;
    *)    # unknown option
    shift # past argument
    ;;
esac
done


buildProject
buildImage
importImage