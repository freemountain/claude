#!/bin/bash
set -ex

ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
TAG_PREFIX="localhost:5000/claude"

buildImage(){
    path="$1"
    dirname=${path##*/} #everything after the final "/"
    tag="$TAG_PREFIX/$dirname"
    docker build -f "$path/Dockerfile" -t "$tag" "$path"
    docker push "$tag"
}

buildAll() {
    for path in "$ROOT/containers"/*/
    do
        path=${path%*/}      # remove the trailing "/"
        buildImage "$path"
    done
}

buildAll
