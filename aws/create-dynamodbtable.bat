aws dynamodb create-table^
 --table-name CloudBill^
 --attribute-definitions AttributeName=Service,AttributeType=S AttributeName=EventDateTime,AttributeType=S^
 --key-schema AttributeName=Service,KeyType=HASH AttributeName=EventDateTime,KeyType=RANGE^
 --billing-mode PAY_PER_REQUEST
