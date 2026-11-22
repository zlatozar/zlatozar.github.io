set shell := ["sh", "-eu", "-c"]

# List available recipes
default:
    @just --list

# Build the Docker image and start the local preview at http://localhost:4000
preview:
    docker compose up --build

# Start the preview server without rebuilding the image
up:
    docker compose up

# Start the preview server in the background
up-bg:
    docker compose up -d

# Stop the preview server
down:
    docker compose down

# Follow preview server logs
logs:
    docker compose logs -f

# Run a one-off Jekyll production build check into _site/
build:
    docker compose run --rm blog jekyll build

# Remove generated _site/ and Jekyll cache files
clean:
    rm -rf _site .jekyll-cache .sass-cache .jekyll-metadata

# Create a new post scaffold in _posts/YYYY-MM-DD-slug.md
new-post slug:
    #!/usr/bin/env sh
    set -eu
    date_prefix=$(date +%Y-%m-%d)
    timestamp=$(date +"%Y-%m-%d %H:%M:%S %z")
    file="_posts/${date_prefix}-{{slug}}.md"
    if [ -e "$file" ]; then
      echo "Already exists: $file" >&2
      exit 1
    fi
    cat <<EOF > "$file"
    ---
    title: "{{slug}}"
    subtitle: ""
    description: ""
    date: ${timestamp}
    categories: []
    tags: []
    ---

    EOF
    echo "Created $file"
