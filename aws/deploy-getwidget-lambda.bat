REM TODO: Parameterize function name and zip file name.
copy ..\build\distributions\aws-crud-0.1-SNAPSHOT.zip .

aws lambda update-function-code --function-name GetWidget --zip-file fileb://aws-crud-0.1-SNAPSHOT.zip

del aws-crud-0.1-SNAPSHOT.zip