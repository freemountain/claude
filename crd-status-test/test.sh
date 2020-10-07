#!/bin/bash
set -ex

CRD_FILE="crd.yaml"
CRD_NAME="stable"

RESOURCE_NAME="myres"
RESOURCE_FILE="stable.yaml"


API_GROUP="foo.com"
FULL_CRD_NAME="$CRD_NAME"s".$API_GROUP"
API_VERSION="v1"
NAMESPACE_BASE_URL="http://127.0.0.1:8001/apis/$API_GROUP/$API_VERSION/namespaces/default"

RESOURCE_URL="$NAMESPACE_BASE_URL/$CRD_NAME"s"/$RESOURCE_NAME"

add_crd() {
    kubectl delete crd "$CRD_NAME"s."$API_GROUP" || true
    kubectl apply -f "$CRD_FILE"
    kubectl get crd "$CRD_NAME"s.foo.com
}

add_resource() {
    kubectl delete "$FULL_CRD_NAME" "$RESOURCE_NAME" || true
    kubectl apply -f "$RESOURCE_FILE"
    kubectl get "$FULL_CRD_NAME" "$RESOURCE_NAME"
}


set_status() {
    res=$(curl "$RESOURCE_URL")
    res_with_status=$(echo $res|jq '. + {status: { field: "value"}}')
    curl -X PUT -H "Content-type: application/json"  -d "$res_with_status" "$RESOURCE_URL/status" | jq .status

}

#add_crd
add_resource
# get_status