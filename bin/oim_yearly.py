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
	print "Failed to load quota_info from OIM. response.status: ",response.status,response.reason
	sys.exit(response.status)
#data = json.JsonReader().read(response.read())

conn.close()
