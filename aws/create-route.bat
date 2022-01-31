set api-id=vlao80eelj
set resource=%1
set method=%2
set integration-id=%3

aws apigatewayv2 create-route^
 --api-id %api-id%^
 --route-key "%method% %resource%"^
 --target integrations/%integration-id%

