CREATE DYNAMODB TABLE
- create-dynamodbtable.bat

CREATE LAMBDA FUNCTIONS
- create-getwidgetlist-lambda.bat
- create-createwidget-lambda.bat
- create-getwidget-lambda.bat
- create-updatewidget-lambda.bat
- create-deletewidget-lambda.bat

CREATE AND CONFIGURE API
- create-api.bat
[need to update scripts with new api id: create-default-stage, create-integration, create-route]

- create-default-stage.bat

(CREATE INTEGRATIONS AND ROUTES)
- create-integration.bat GetWidgetList
- create-getwidgetlist-route.bat <integration id for GetWidgetList>

- create-integration.bat CreateWidget
- create-createwidget-route.bat <integration id for CreateWidget>

- create-integration.bat GetWidget
- create-getwidget-route.bat <integration id for GetWidget>

- create-integration.bat UpdateWidget
- create-updatewidget-route.bat <integration id for UpdateWidget>

- create-integration.bat DeleteWidget
- create-deletewidget-route.bat <integration id for DeleteWidget>



UPDATE LAMBDA FUNCTION (as required)
- deploy-lambda <function-name>