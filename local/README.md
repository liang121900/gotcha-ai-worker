To run the app locally and connect to sqs locally, 
we can use localstack.
Please install localstack first (require python)
```
pip install localstack
```
Run the localstack in docker and create sqs queue, or simply run the create-local-aws-resource.py script which for windows requires wsl
```
localstack start -d
docker container exec -it {containerId} bash
awslocal sqs create-queue --queue-name gotcha-detection-request-queue-local
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/gotcha-detection-request-queue-local --message-body test
```
paste the output url to the local yaml file
To test:
do a post on http://localhost:4566/gotcha-detect-queue-local?Action=SendMessage&MessageBody={"a":"b"}
use the postman collection in postman folder
