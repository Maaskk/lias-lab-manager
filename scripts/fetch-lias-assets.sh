#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="$ROOT_DIR/backend/uploads/public"
mkdir -p "$TARGET"

echo "Downloading public LIAS assets into $TARGET"
curl -L --fail "https://lias.ma/images/Logo-LIAS-01.png" -o "$TARGET/Logo-LIAS-01.png"
curl -L --fail "https://lias.ma/images/Program_ICAIS25.pdf" -o "$TARGET/Program_ICAIS25.pdf"

echo "Done. Public assets saved:"
ls -lh "$TARGET"
