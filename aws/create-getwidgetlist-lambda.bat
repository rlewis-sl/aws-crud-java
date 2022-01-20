REM TODO: Parameterize function name, handler name and zip file name.
copy ..\build\distributions\aws-crud-0.1-SNAPSHOT.zip .

aws lambda create-function^
 --function-name GetWidgetList^
 --runtime java11^
 --role arn:aws:iam::184936849605:role/Lambda_WidgetCrud^
 --timeout 30^
 --memory-size 256^
 --handler com.algopop.awscrud.GetListHandler::handleRequest^
 --zip-file fileb://aws-crud-0.1-SNAPSHOT.zip

del aws-crud-0.1-SNAPSHOT.zip