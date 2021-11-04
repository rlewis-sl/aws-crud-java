aws apigatewayv2 create-api^
 --name SLDemo_API^
 --protocol-type HTTP

REM does not work for me ...  --target arn:aws:lambda:eu-west-1:184936849605:function:SLDemo_Function
REM so have to manually link api as trigger for SLDemo_Function in aws lambda web console
