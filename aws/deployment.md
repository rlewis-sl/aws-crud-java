CREATE DYNAMODB TABLE
> create-dynamodbtable.bat

CREATE LAMBDA FUNCTIONS
> create-getwidgetlist-lambda.bat
> create-createwidget-lambda.bat
> create-getwidget-lambda.bat
> create-updatewidget-lambda.bat
> create-deletewidget-lambda.bat

CREATE AND CONFIGURE API
> create-api.bat
[need to update scripts below with new api id]

> create-default-stage.bat

(CREATE INTEGRATIONS)
> create-integration.bat GetWidgetList
> create-integration.bat CreateWidget
> create-integration.bat GetWidget
> create-integration.bat UpdateWidget
> create-integration.bat DeleteWidget

(ADD LAMBDA EXECUTE PERMISSIONS)  // TODO: combine these with create-integration scripts above
> add-lambda-permission GetWidgetList
> add-lambda-permission CreateWidget
> add-lambda-permission GetWidget
> add-lambda-permission UpdateWidget
> add-lambda-permission DeleteWidget

(CREATE ROUTES)
> create-getwidgetlist-route.bat <integration id for GetWidgetList>
> create-createwidget-route.bat <integration id for CreateWidget>
> create-getwidget-route.bat <integration id for GetWidget>
> create-updatewidget-route.bat <integration id for UpdateWidget>
> create-deletewidget-route.bat <integration id for DeleteWidget>

UPDATE LAMBDA FUNCTION (as required)
> deploy-lambda <function-name>