#!/bin/bash
set -ex

toUppercase() {
    echo "$@" | tr '[:lower:]' '[:upper:]'
}

join() {
    delimiter="$1"
    echo "${@:2}" | sed "s/ /$delimiter/g"
}

mapPrivileges() {
    if [ "$(toUppercase "$1")" = "ALL" ]; then
        privileges="ALL PRIVILEGES"
    else
        privileges="$(join ", " "$@")"
    fi
    echo "$privileges"
}

executeQuery() {
    query="$*"
    mysql -h "$DB_HOST" -P "$DB_PORT" -p"$DB_ROOT_PASSWORD" -u "$DB_ROOT_USER" -e "$query"
}

createDatabase() {
    database="$1"
    executeQuery "CREATE DATABASE IF NOT EXISTS \`$1\`;"
}

createUser() {
    username="$1"
    password="$2"
    alterUserQuery="ALTER USER IF EXISTS '$username'@'%' IDENTIFIED BY '$password';"
    createUserQuery="CREATE USER IF NOT EXISTS '$username'@'%' IDENTIFIED BY '$password';"
    executeQuery "$alterUserQuery" "$createUserQuery"
}

grantPrivileges() {
    database="$1"
    username="$2"
    privileges="$(mapPrivileges "${@:3}")"

    executeQuery "GRANT $privileges ON \`$database\`.* TO '$username'@'%';"
}

createUserWithPrivileges() {
    database="$1"
    username="$2"
    password="$3"
    privileges="$(mapPrivileges "${@:4}")"

    alterUserQuery="ALTER USER IF EXISTS '$username'@'%' IDENTIFIED BY '$password';"
    createUserQuery="CREATE USER IF NOT EXISTS '$username'@'%' IDENTIFIED BY '$password';"
    grantPrivilegesQuery="GRANT $privileges ON \`$database\`.* TO '$username'@'%';"

    executeQuery "$alterUserQuery" "$createUserQuery" "$grantPrivilegesQuery"
}


case $1 in
	createDatabase)
		createDatabase "${@:2}"
		;;
	createUser)
		createUser "${@:2}"
		;;
	grantPrivileges)
		grantPrivileges "${@:2}"
		;;
    createUserWithPrivileges)
		createUserWithPrivileges "${@:2}"
		;;
    *)
        >&2 echo "unknown command '$1'"
        exit 1
esac