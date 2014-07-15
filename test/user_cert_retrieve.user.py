#!/usr/bin/python

import urllib
import httplib
import M2Crypto
import time

ctx = M2Crypto.SSL.Context()
ctx.load_cert("/home/hayashis/.globus/soichi.2014.pem")

headers = {'Content-type': "application/x-www-form-urlencoded", 'User-Agent': 'OIMGridAPIClient/0.1 (OIM Grid API)'}
conn = M2Crypto.httpslib.HTTPSConnection("oim-itb.grid.iu.edu", ssl_context=ctx)

params = urllib.urlencode({'user_request_id': "938"}, doseq=True)
conn.request('POST', "/oim/rest?action=user_cert_retrieve", params, headers)
response = conn.getresponse()
print response.status, response.reason
data = response.read()
print data
cookie = response.getheader("set-cookie")
print cookie

conn.close()

