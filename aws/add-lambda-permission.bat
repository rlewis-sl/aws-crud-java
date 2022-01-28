set api-id=acnncd1npk
set function-name=%1

aws lambda add-permission^
 --statement-id EnableApiLambda%function-name%Integration^
 --action lambda:InvokeFunction^
 --function-name "arn:aws:lambda:eu-west-1:184936849605:function:%function-name%"^
 --principal apigateway.amazonaws.com^
 --source-arn "arn:aws:execute-api:eu-west-1:184936849605:%api-id%%/*"
