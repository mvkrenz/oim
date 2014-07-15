#!/usr/bin/python
import urllib
import httplib
import time

import simplejson
import base64

request_id = 3931

headers = {'Content-type': "application/x-www-form-urlencoded", 'User-Agent': 'OIMGridAPIClient/0.1 (OIM Grid API)'}
#conn = M2Crypto.httpslib.HTTPSConnection("oim-itb.grid.iu.edu", ssl_context=ctx)
conn = httplib.HTTPConnection("oim-itb.grid.iu.edu:80")
conn.set_debuglevel(1)
params = urllib.urlencode({'host_request_id': request_id}, doseq=True)

conn.request('POST', "/oim/rest?action=host_certs_retrieve", params, headers)
response = conn.getresponse()
data = simplejson.loads(response.read())
cookie = response.getheader("set-cookie")

#debug
print response.status, response.reason
print data

idx=0
for pkcs7 in data["pkcs7s"]:
    name = "/tmp/hostcert.%d.%d.pem" % (request_id, idx)
    print name
    f = open(name, "w")
    f.write(pkcs7)
    f.close()
    idx+=1
 
conn.close()
