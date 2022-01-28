set resource=/widgets/{id}
set method=PUT
set integration-id=%1

create-route %resource% %method% %integration-id%
