#!/usr/bin/python

import urllib
import httplib
import M2Crypto
import time

import simplejson
import base64

ctx = M2Crypto.SSL.Context()
ctx.load_cert("/home/hayashis/.globus/soichi.2014-2015.pem")

headers = {'Content-type': "application/x-www-form-urlencoded", 'User-Agent': 'OIMGridAPIClient/0.1 (OIM Grid API)'}

serial_id = "03FE6B7F6D355317A5B8B87487197B5A"; #old
#serial_id = "non-existing";

print "renewing"
conn = M2Crypto.httpslib.HTTPSConnection("oim-itb.grid.iu.edu", ssl_context=ctx)
conn.set_debuglevel(1)
params = urllib.urlencode({'serial_id': serial_id, 'password': 'new password needs to be strong #4069'}, doseq=True)
conn.request('POST', "/oim/rest?action=user_cert_renew", params, headers)
ssl_session = conn.get_session()
response = conn.getresponse()
data = response.read()
cookie = response.getheader("set-cookie")

#debug
print "STATUS:",response.status, response.reason
print cookie
print "DATA:",data
print ssl_session.as_text()

print "waiting for 20 seconds"
time.sleep(20)

print "retrieving - until it's issued"
headers["Cookie"] = cookie #for server session id
params = urllib.urlencode({'user_request_id': request_id}, doseq=True)
conn = M2Crypto.httpslib.HTTPSConnection("oim-itb.grid.iu.edu", ssl_context=ctx) #I need to reconnect for some reason..
conn.set_debuglevel(1)
conn.set_session(ssl_session) #probably not needed, but just to be nice to the web server
conn.request('POST', "/oim/rest?action=user_cert_retrieve", params, headers)
response = conn.getresponse()
json = response.read()

#debug
print response.status, response.reason
print json

#pull pkcs12 and decode
data = simplejson.loads(json)
pkcs12 = base64.b64decode(data["pkcs12"])
f = open("user.p12", "w")
f.write(pkcs12)
f.close()

conn.close()

