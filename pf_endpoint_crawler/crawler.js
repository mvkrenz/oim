
var https = require('https');
var http = require('http');
var fs = require('fs');

var _ = require('lodash');
var async = require('async');
var xml2js = require('xml2js');
var ps =  require('perfsonar');

var config = require('./config.json');

/*
function testhead(hostname, port, path, cb) {
    var options = {method: 'HEAD', host: hostname, port: port, path: path};
    console.dir(options);
    var req = http.request(options, function(res) {
        //console.log("status:"+res.statusCode);
        cb(res.statusCode);
    }).on('error', function(e) {
        //console.error("request error-ed");
        //console.dir(e);
        cb(null);
    });
    req.setTimeout(3000, function() {
        //console.error("timeout - aborting");
        req.abort();
    });
    req.end();
}
*/

exports.find_mas = function(hostname, cb) {
    console.log("testing "+hostname);
    var mas = [];
    async.series([
        function(next) {
            //this find most of 3.2 endpoints
            ps.ma.endpoint({host: hostname}, function(err, endpoints) {
                if(err) return next();
                if(endpoints.pinger != null) {
                    mas.push({
                        read_url: 'http://'+hostname+':8075/perfSONAR_PS/services/pinger/ma',
                        type: 'pinger'
                    });
                }
                if(endpoints.traceroute != null) {
                    mas.push({
                        read_url: 'http://'+hostname+':8086/perfSONAR_PS/services/tracerouteMA',
                        write_url: 'http://'+hostname+':8086/perfSONAR_PS/services/tracerouteCollector',
                        type: 'traceroute'
                    });
                }
                if(endpoints.owamp != null) {
                    mas.push({
                        read_url: 'http://'+hostname+':8085/perfSONAR_PS/services/pSB',
                        write_url: hostname+':8569',
                        type: 'perfsonarbuoy/owamp'
                    });
                }
                if(endpoints.iperf != null) {
                    mas.push({
                        read_url: 'http://'+hostname+':8085/perfSONAR_PS/services/pSB',
                        write_url: hostname+':8570',
                        type: 'perfsonarbuoy/bwctl'
                    });
                }

                //TODO - now test for 3.3 endpoints

                next();
            });
        }
    ], function(err) {
        cb(null, mas); 
    });
}


