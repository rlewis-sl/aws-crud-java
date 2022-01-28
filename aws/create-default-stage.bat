set api-id=acnncd1npk

aws apigatewayv2 create-stage^
 --api-id %api-id%^
 --auto-deploy^
 --stage-name $default
