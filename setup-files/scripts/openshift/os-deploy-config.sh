#!/bin/bash

set -e

PROJECT=$1
TARGET=$2
VERSION=$3

. $(dirname $0)/deploy-functions.sh
. $(dirname $0)/local-deploy-functions.sh

PROJECT=$(getProjectName $PROJECT $TARGET)
SVC_ACCOUNT_TOKEN=$(getSvcAccountToken)
HOST=$(getHost $TARGET)
ROUTE_DOMAIN=$(getRouteDomain $TARGET $HOST)
REGISTRY=$(getRegistry)
INTERNAL_REGISTRY=$(getInternalRegistry)
SVC_ACCOUNT_CLAUSE=$(getSvcAccountClause $TARGET $PROJECT $SVC_ACCOUNT_TOKEN)
REGISTRY_TOKEN=$SVC_ACCOUNT_TOKEN

echo "Applying the config maps for $PROJECT Openshift project"

function applyConfigMaps {
    oc apply -f $(getBuildLocation)/acc-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/cache-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/data-service-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/db-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/finance-db-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/flyway-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/grant-db-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/ldap-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/idp-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/new-relic-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/performance-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/shibboleth-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/survey-db-config.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f $(getBuildLocation)/web-config.yml ${SVC_ACCOUNT_CLAUSE}
}

# Entry point
applyConfigMaps

