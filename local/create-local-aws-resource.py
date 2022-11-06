#!/usr/bin/env python3
import os
import subprocess

os.system("localstack start -d")
os.system("docker container exec localstack_main awslocal sqs create-queue --queue-name gotcha-detection-request-queue-local
")