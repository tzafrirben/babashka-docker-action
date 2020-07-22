#!/bin/sh -l

echo "first $1"
echo "second $2"
echo "third $3"
time=$(date)
echo "::set-output name=output::$time"