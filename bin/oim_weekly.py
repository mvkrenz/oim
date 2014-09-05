#!/usr/bin/python
import urllib
import httplib
import json
import sys

conn = httplib.HTTPConnection("t520", 8080)
conn.request("POST", "/oim/rest?action=notify_expiring_cert_request&version=1")
response = conn.getresponse()
if response.status != 200:
        print "Failed to run notify_expiring_cert_request ito OIM. response.status: ",response.status,response.reason
        sys.exit(response.status)
else:
	data = json.loads(response.read())
	status = data["status"]
	if status != "OK":
		print "notify_expiring_cert_request failed:", data["detail"]

conn.close()
