#!/usr/bin/python
import urllib
import httplib
import json
import sys

#do quota daly reset
conn = httplib.HTTPConnection("localhost")
conn.request("POST", "/oim/rest?action=reset_daily_quota&version=1")
response = conn.getresponse()
if response.status != 200:
	print "Failed to load quota_info from OIM. response.status: ",response.status,response.reason
	sys.exit(response.status)
#data = json.JsonReader().read(response.read())

#process expired certs
conn.request("POST", "/oim/rest?action=find_expired_cert_request&version=1")
response = conn.getresponse()
if response.status != 200:
	print "Failed to load quota_info from OIM. response.status: ",response.status,response.reason
	sys.exit(response.status)
#data = json.JsonReader().read(response.read())

conn.close()
