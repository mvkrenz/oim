#!/usr/bin/python
import urllib
import httplib
import json
import sys

conn = httplib.HTTPConnection("localhost")
conn.request("POST", "/oim/rest?action=notify_expiring_cert_request&version=1")
response = conn.getresponse()
if response.status != 200:
        print "Failed to run notify_expiring_cert_request ito OIM. response.status: ",response.status,response.reason
        sys.exit(response.status)
conn.close()

