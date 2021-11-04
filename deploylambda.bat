REM TODO: Parameterize function name and zip file name.
aws lambda update-function-code --function-name SLDemo_Function --zip-file fileb://build/distributions/lambda-one-0.1-SNAPSHOT.zip
