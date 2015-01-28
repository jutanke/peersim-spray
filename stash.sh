#!/bin/sh

for f; do
    mv "$f" "${f%.java}._java"
done
