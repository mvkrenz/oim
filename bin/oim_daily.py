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
	print "reset_daily_quota (request) failed:",response.status,response.reason
	sys.exit(response.status)
else:
	data = json.loads(response.read())
	status = data["status"]
	if status != "OK":
		print "reset_daily_quota failed:", data["detail"]

#process expired certs
conn.request("POST", "/oim/rest?action=find_expired_cert_request&version=1")
response = conn.getresponse()
if response.status != 200:
	print "find_expired_cert_request (request) failed: ",response.status,response.reason
	sys.exit(response.status)
else:
	data = json.loads(response.read())
	status = data["status"]
	if status != "OK":
		print "find_expired_cert_request failed:", data["detail"]

conn.close()
