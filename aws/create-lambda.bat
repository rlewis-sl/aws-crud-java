REM TODO: Parameterize function name, handler name and zip file name.
copy ..\build\distributions\lambda-one-0.1-SNAPSHOT.zip .

aws lambda create-function^
 --function-name SLDemo_Function^
 --runtime java11^
 --role arn:aws:iam::184936849605:role/SLDemo_Lambda^
 --handler com.algopop.lambdaone.Handler::handleRequest^
 --zip-file fileb://lambda-one-0.1-SNAPSHOT.zip

del lambda-one-0.1-SNAPSHOT.zip