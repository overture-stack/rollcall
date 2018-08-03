#!/bin/bash

branch_or_tag=master
image_name=rollcall-1.0

docker build --build-arg "TAG_NAME=$branch_or_tag" --tag $image_name ./
