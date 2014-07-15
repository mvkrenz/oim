#!/usr/bin/python

import urllib
import httplib
import M2Crypto
import time

import simplejson
import base64

ctx = M2Crypto.SSL.Context()
ctx.load_cert("/home/hayashis/.globus/soichi.2014.pem")

headers = {'Content-type': "application/x-www-form-urlencoded", 'User-Agent': 'OIMGridAPIClient/0.1 (OIM Grid API)'}

conn = M2Crypto.httpslib.HTTPSConnection("oim-itb.grid.iu.edu", ssl_context=ctx)
conn.set_debuglevel(1)
params = urllib.urlencode({'serial_id': '02:31:4a:3b:da:d7:b4:cc:b6:90:ff:92:7b:62:f4:a7'}, doseq=True)
conn.request('POST', "/oim/rest?action=host_certs_revoke", params, headers)
ssl_session = conn.get_session()
response = conn.getresponse()
data = response.read()
cookie = response.getheader("set-cookie")

#debug
print "STATUS:",response.status, response.reason
print cookie
print "DATA:",data
print ssl_session.as_text()

conn.close()

