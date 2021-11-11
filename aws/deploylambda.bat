REM TODO: Parameterize function name and zip file name.
copy ..\build\distributions\lambda-one-0.1-SNAPSHOT.zip .

aws lambda update-function-code --function-name SLDemo_Function --zip-file fileb://lambda-one-0.1-SNAPSHOT.zip

del lambda-one-0.1-SNAPSHOT.zip