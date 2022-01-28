REM TODO: Parameterise the lambda function id used as target.

aws apigatewayv2 create-api^
 --name WidgetCrud^
 --protocol-type HTTP^
 --cors-configuration AllowOrigins="*"

REM --target arn:aws:lambda:eu-west-1:184936849605:function:GetWidgetList

REM Line below adds permissions to enable the integration with the lambda IF YOU KNOW THE API ID!!
REM aws lambda add-permission --statement-id EnableApiLambdaIntegration --action lambda:InvokeFunction --function-name "arn:aws:lambda:eu-west-1:184936849605:function:SLDemo_Function" --principal apigateway.amazonaws.com --source-arn "arn:aws:execute-api:eu-west-1:184936849605:<api-id>/*"

REM Line below adds a route to match for an integration with the lambda (if you know the api id)
REM aws apigatewayv2 create-route --api-id arn:aws:execute-api:eu-west-1:184936849605:<api-id> --routeKey 'GET /widgets'

REM Still need commands to attach a route to an integration (requires api id, route id an integration id)
