import os

os.system("localstack start -d")
os.system(
    "docker container exec localstack_main awslocal sqs create-queue --queue-name gotcha-detection-request-queue-local")
os.system(
    "docker container exec localstack_main awslocal s3api create-bucket --bucket gotcha-detection-input-local")
os.system(
    "docker container exec localstack_main awslocal s3api create-bucket --bucket gotcha-detection-output-local")