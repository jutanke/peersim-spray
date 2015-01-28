#!/bin/sh

for f; do
    mv "$f" "${f%._java}.java"
done
