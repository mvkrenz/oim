#!/usr/bin/python
import urllib
import httplib
import json
import sys

#do quota yearly reset
conn = httplib.HTTPConnection("localhost")
conn.request("POST", "/oim/rest?action=reset_yearly_quota&version=1")
response = conn.getresponse()
if response.status != 200:
	print "reset_yearly_quota (request) failed:",response.status,response.reason
	sys.exit(response.status)
else:
	data = json.loads(response.read())
	status = data["status"]
	if status != "OK":
		print "reset_yearly_quota failed:", data["detail"]

conn.close()
