set api-id=acnncd1npk
set function-name=%1
set method=GET

aws apigatewayv2 create-integration^
 --api-id %api-id%%^
 --integration-type AWS_PROXY^
 --integration-uri arn:aws:lambda:eu-west-1:184936849605:function:%function-name%^
 --payload-format-version 2.0^
 --integration-method %method%

