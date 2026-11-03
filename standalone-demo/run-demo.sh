#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
javac --release 17 -d out src/BillingCopilotDemo.java
java -cp out BillingCopilotDemo --test
java -cp out BillingCopilotDemo 8787
