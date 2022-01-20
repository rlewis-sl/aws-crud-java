REM create dynamodb table with partition key, and no sort key
aws dynamodb create-table^
 --table-name Widget^
 --attribute-definitions AttributeName=Id,AttributeType=S^
 --key-schema AttributeName=Id,KeyType=HASH^
 --billing-mode PAY_PER_REQUEST
