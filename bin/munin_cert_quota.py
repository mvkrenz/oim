#!/usr/bin/python
import urllib
import httplib
import json
import sys

#load quota info
conn = httplib.HTTPConnection("localhost")
conn.request("GET", "/oim/rest?action=quota_info&version=1")
response = conn.getresponse()
if response.status != 200:
	print "Failed to load quota_info from OIM. response.status: ",response.status,response.reason
	sys.exit(response.status)
data = json.loads(response.read())
conn.close()

if data["status"] != "OK":
	print "quota_info returned on-OK:", data["status"]
	sys.exit(response.status)

#data["global_usercert_year_count"]
#data["global_usercert_year_max"]
#data["global_hostcert_year_count"]
#data["global_hostcert_year_max"]

if len(sys.argv) > 1:
	#handle config request
	if sys.argv[1] == "autoconf":
		print "yes";

	if sys.argv[1] == "config":
		print "graph_title OIM Quota Information"
		print "graph_vlabel requests"
		print "graph_category quota"
		print "global_usercert_year_count.label User Cert Requests"
		print "global_usercert_year_count.info Global User Certificate Yearly Count"
		print "global_usercert_year_count.max",data["global_usercert_year_max"]
		print "global_usercert_year_count.critical",data["global_usercert_year_max"]*0.75
		print "global_usercert_year_count.warning",data["global_usercert_year_max"]*0.50

		print "global_hostcert_year_count.label Host Cert Requests"
		print "global_hostcert_year_count.info Global Host Certificate Yearly Count"
		print "global_hostcert_year_count.max",data["global_hostcert_year_max"]
		print "global_hostcert_year_count.critical",data["global_hostcert_year_max"]*0.75
		print "global_hostcert_year_count.warning",data["global_hostcert_year_max"]*0.50
else:
	print "global_usercert_year_count.value",data["global_usercert_year_count"]	
	print "global_hostcert_year_count.value",data["global_hostcert_year_count"]	
