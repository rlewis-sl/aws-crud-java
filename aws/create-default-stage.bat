set api-id=vlao80eelj

aws apigatewayv2 create-stage^
 --api-id %api-id%^
 --auto-deploy^
 --stage-name $default
