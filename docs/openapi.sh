#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIRECTORY="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$(cd -- "${SCRIPT_DIRECTORY}/.." && pwd)"
readonly OPENAPI_SNAPSHOT="${PROJECT_ROOT}/docs/openapi.json"
readonly REDOC_HTML="${PROJECT_ROOT}/src/main/resources/static/redoc.html"
readonly REDOCLY_IMAGE="redocly/cli:2.40.0"
readonly DEFAULT_OPENAPI_URL="http://localhost:8080/management/rest/v3/api-docs"

usage() {
    echo "Uso: ./docs/openapi.sh {export|check|lint|build|all}"
}

require_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "Comando obrigatório não encontrado: $1" >&2
        exit 1
    fi
}

require_credentials() {
    if [[ -z "${APP_SECURITY_USERNAME:-}" || -z "${APP_SECURITY_PASSWORD:-}" ]]; then
        echo "Defina APP_SECURITY_USERNAME e APP_SECURITY_PASSWORD para acessar o OpenAPI." >&2
        exit 1
    fi
}

fetch_openapi() (
    local output_file="$1"
    local raw_file

    require_command curl
    require_command jq
    require_credentials

    raw_file="$(mktemp)"
    trap 'rm -f "${raw_file}"' EXIT

    curl \
        --fail \
        --silent \
        --show-error \
        --user "${APP_SECURITY_USERNAME}:${APP_SECURITY_PASSWORD}" \
        "${OPENAPI_URL:-${DEFAULT_OPENAPI_URL}}" \
        --output "${raw_file}"

    jq -S . "${raw_file}" > "${output_file}"
)

run_redocly() {
    require_command docker

    docker run \
        --rm \
        --user "$(id -u):$(id -g)" \
        --volume "${PROJECT_ROOT}:/spec" \
        --workdir /spec \
        "${REDOCLY_IMAGE}" \
        "$@"
}

export_snapshot() (
    local temporary_snapshot

    temporary_snapshot="$(mktemp)"
    trap 'rm -f "${temporary_snapshot}"' EXIT
    fetch_openapi "${temporary_snapshot}"
    mv "${temporary_snapshot}" "${OPENAPI_SNAPSHOT}"
    chmod 0644 "${OPENAPI_SNAPSHOT}"
    echo "Snapshot atualizado: ${OPENAPI_SNAPSHOT}"
)

check_snapshot() (
    local current_snapshot
    local normalized_snapshot

    current_snapshot="$(mktemp)"
    normalized_snapshot="$(mktemp)"
    trap 'rm -f "${current_snapshot}" "${normalized_snapshot}"' EXIT

    fetch_openapi "${current_snapshot}"
    jq -S . "${OPENAPI_SNAPSHOT}" > "${normalized_snapshot}"

    if ! diff -u "${normalized_snapshot}" "${current_snapshot}"; then
        echo "O snapshot OpenAPI está desatualizado. Execute ./docs/openapi.sh export." >&2
        exit 1
    fi

    echo "Snapshot OpenAPI sincronizado."
)

lint_openapi() {
    run_redocly check-config
    run_redocly lint payable-management@v1
}

build_redoc() {
    run_redocly build-docs docs/openapi.json --output=src/main/resources/static/redoc.html
    echo "Redoc gerado: ${REDOC_HTML}"
}

case "${1:-}" in
    export)
        export_snapshot
        ;;
    check)
        check_snapshot
        ;;
    lint)
        lint_openapi
        ;;
    build)
        build_redoc
        ;;
    all)
        export_snapshot
        lint_openapi
        build_redoc
        ;;
    *)
        usage
        exit 1
        ;;
esac
