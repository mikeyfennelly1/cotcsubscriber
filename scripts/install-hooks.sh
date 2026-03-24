#!/bin/bash
# Installs git hooks by symlinking from scripts/hooks/ into .git/hooks/

set -e

REPO_ROOT=$(git rev-parse --show-toplevel)
HOOKS_SRC="$REPO_ROOT/scripts/hooks"
HOOKS_DEST="$REPO_ROOT/.git/hooks"

for hook in "$HOOKS_SRC"/*; do
    name=$(basename "$hook")
    dest="$HOOKS_DEST/$name"

    chmod +x "$hook"

    if [ -L "$dest" ]; then
        rm "$dest"
    elif [ -f "$dest" ]; then
        echo "WARNING: Backing up existing $name hook to $dest.bak"
        mv "$dest" "$dest.bak"
    fi

    ln -s "$hook" "$dest"
    echo "Installed $name hook"
done

echo "Done. Hooks installed."
