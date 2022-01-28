set resource=/widgets/{id}
set method=DELETE
set integration-id=%1

create-route %resource% %method% %integration-id%
