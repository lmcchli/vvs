#!/bin/bash

# Define your directory (root of the project)
root_dir="./"


find "$root_dir" -type f \( -type f \) | while read -r file; do
    #echo "Updating file: $file"
    sed -i 's/ABCXYZ/ABCXYZ/g' "$file"
done

echo "Replacement complete."

