aws apigatewayv2 create-api^
 --name SLDemo_API^
 --protocol-type HTTP^
 --target arn:aws:lambda:eu-west-1:184936849605:function:SLDemo_Function

REM Line below adds permissions to enable the integration with the lambda IF YOU KNOW THE API ID!!
REM aws lambda add-permission --statement-id EnableApiLambdaIntegration --action lambda:InvokeFunction --function-name "arn:aws:lambda:eu-west-1:184936849605:function:SLDemo_Function" --principal apigateway.amazonaws.com --source-arn "arn:aws:execute-api:eu-west-1:184936849605:<api-id>/*"
