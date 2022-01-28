REM TODO: Parameterize zip file name.

set function-name=%1

copy ..\build\distributions\aws-crud-0.1-SNAPSHOT.zip .

aws lambda update-function-code --function-name %function-name% --zip-file fileb://aws-crud-0.1-SNAPSHOT.zip

del aws-crud-0.1-SNAPSHOT.zip
