set resource=/widgets/{id}
set method=GET
set integration-id=%1

create-route %resource% %method% %integration-id%
