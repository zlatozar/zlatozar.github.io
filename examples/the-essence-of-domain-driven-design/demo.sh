#!/bin/bash
#------------------------------------------------------------------------------
# demo.sh - Send one blunt scalpel through the three bounded contexts
#
# 2026-11-28, Zlatozar Zhelyazkov <zlatozar@gmail.com>
#
# Copyright (c) 2026 Zlatozar Zhelyazkov. MIT License, see LICENSE in the
# repository root.
#------------------------------------------------------------------------------
#
# Plays the repair story by the public APIs, as RepairFlowIT does:
#
#   Department  -- event -->  Workshop  -- event -->  Accounting
#   send, approve             start, outcome          repair cost
#
# At the end the department takes the scalpel back into use.
#
# Start the application first (`just run`). Needs curl 7.82+ (for --json) and jq.
# Run it again and the repair costs add up: the scalpel becomes uneconomical.
#

# Debug script
#set -x

set -euo pipefail

# Keep track of program name for reference, removing absolute path
PROGNAME=${0##*/}

DEFAULT_API=http://localhost:8080
INSTRUMENT=SC-1042
# A step waits at most MAX_TRIES x PAUSE_SECONDS (5 s) for an event
MAX_TRIES=50
PAUSE_SECONDS=0.1
BODY=""

# clean_exit: Remove the temporary file and exit
# @param $1 - Exit status (Optional). Use $?, if not set
clean_exit() {
    local estatus=${1:-$?}
    trap - EXIT
    rm -f "$BODY"
    exit "$estatus"
}

# die: Exit with error
# @params $* - Reason for exiting
die() {
    echo "$PROGNAME: $*" >&2
    clean_exit 1
}

# usage: Script usage message
# @params - NONE
usage() {
    cat << EOU
Usage: $PROGNAME [-h] [API_URL]

Sends one blunt scalpel through the department, the workshop and accounting.

OPTIONS:
    -h    Show this help

API_URL is where the application runs, $DEFAULT_API by default.

Example Usage:
    $PROGNAME
    $PROGNAME http://localhost:8090
EOU
}

# check_requirements: Stop early, with a clear message, if a tool or the
#                     application is missing
# @params - NONE
check_requirements() {
    command -v jq > /dev/null || die "jq is missing, see https://jqlang.org/"
    curl -fsS -o /dev/null "$API/scalar" 2> /dev/null \
        || die "No application at $API. Start it first: just run"
}

# call: Send a request and print it with its status. On a 4xx or 5xx print
#       the problem and stop.
# @param $1 - HTTP method
# @param $2 - Path
# @params $3... - Extra curl options, such as --json
call() {
    local method=$1 path=$2 status
    shift 2
    status=$(curl -sS -X "$method" -o "$BODY" -w '%{http_code}' "$@" "$API$path")
    echo "  $method $path -> $status"
    if [ "$status" -ge 400 ]; then
        cat "$BODY" >&2
        echo >&2
        die "$method $path failed"
    fi
}

# wait_until: The contexts talk by events after a commit, so a step waits
#             until the event has arrived
# @params $* - Command that succeeds when the event has arrived
wait_until() {
    local tries=0
    until "$@"; do
        tries=$((tries + 1))
        [ "$tries" -lt "$MAX_TRIES" ] || die "Gave up waiting: $*"
        sleep "$PAUSE_SECONDS"
    done
}

repair_cost() { curl -fsS "$API/assets/$INSTRUMENT" | jq -r .repairCost.amount; }
cost_has_changed() { [ "$(repair_cost)" != "$COST_BEFORE" ]; }
job_has_arrived() { curl -fs -o /dev/null "$API/repair-jobs/$JOB"; }

#------------------------------------------------------------------------------
# Main Routine
#------------------------------------------------------------------------------

while getopts ":h" option; do
    case "$option" in
        h)
            usage
            exit 0
            ;;
        *)
            echo "$PROGNAME: Error - invalid option -$OPTARG" >&2
            echo >&2
            usage >&2
            exit 1
            ;;
    esac
done
shift $((OPTIND - 1))

API=${1:-$DEFAULT_API}
BODY=$(mktemp)
trap clean_exit EXIT
trap 'clean_exit 1' INT HUP QUIT TERM

check_requirements
COST_BEFORE=$(repair_cost)

echo "Department: the nurse sends the blunt scalpel $INSTRUMENT for repair"
call POST "/departments/SURGERY/instruments/$INSTRUMENT/repair" \
    --json '{"kind": "BLUNT", "note": "Does not cut"}'
BOX=$(curl -fsS "$API/departments/SURGERY" |
    jq -r --arg id "$INSTRUMENT" '.openBoxes[] | select(any(.items[]; .instrument == $id)) | .id')

echo "Department: the director approves repair box $BOX"
call POST "/departments/SURGERY/repair-boxes/$BOX/approval"

JOB="SURGERY-$BOX-$INSTRUMENT"
echo "Workshop: the box arrives as job $JOB, and the technician sharpens the scalpel"
wait_until job_has_arrived
call POST "/repair-jobs/$JOB/start"
call POST "/repair-jobs/$JOB/outcome" \
    --json '{"result": "REPAIRED", "cost": {"amount": "45.50", "currency": "EUR"}}'

echo "Accounting: the repair is added to what the scalpel has cost"
wait_until cost_has_changed
curl -fsS "$API/assets/$INSTRUMENT" | jq .

echo "Department: the scalpel comes back and can be used again"
call POST "/departments/SURGERY/instruments/$INSTRUMENT/return"
curl -fsS "$API/departments/SURGERY" | jq --arg id "$INSTRUMENT" '.instruments[] | select(.id == $id)'

# __END__
