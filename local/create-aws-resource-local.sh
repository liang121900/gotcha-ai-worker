localstack start -d
docker container exec localstack_main awslocal sqs create-queue --queue-name gotcha-detection-request-queue-local
