#!/bin/bash
set -ex

ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
TAG_PREFIX="localhost:5000/claude"
BUILD_TYPE=jvm

buildProject() {
    if [ "$BUILD_TYPE" == "native" ]; then
        mvn -f "$ROOT/operator/" package -Pnative -DskipTests -Dnative-image.docker-build=true
    else
        mvn -f "$ROOT/operator/" package -DskipTests
    fi
}

buildImage() {
    tag="$TAG_PREFIX/operator-$BUILD_TYPE"
    docker build -f "$ROOT/operator/src/main/docker/Dockerfile.$BUILD_TYPE" -t "$tag" "$ROOT/operator/"
    docker push "$tag"
}


while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -t|--type)
    BUILD_TYPE="$2"
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