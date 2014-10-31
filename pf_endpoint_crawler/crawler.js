
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
        /*
        //this find most of 3.2/3.3 endpoints
        function(next) {
            ps.ma.endpoint({host: hostname}, function(err, endpoints) {
                if(err) return next(); //ignore it
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
                next();
            });
        },
        */
        
        //find 3.4 endpoint
        function(next) {
            var url = "http://"+hostname+"/toolkit/?format=json";
            //console.log("testing esdmond url:"+url);
            http.get(url, function(res) {
                //console.dir(res);
                var body = "";
                res.on('data', function(chunk) {
                    body += chunk;
                });
                res.on('end', function() {
                    //console.dir(res);
                    if(res.statusCode == "200") {
                        try {
                            var info = JSON.parse(body);
                            //iterate services
                            var maurl = null;
                            var running = [];
                            info.services.forEach(function(service) {
                                if(service.is_running == "yes") {
                                    switch(service.name) {
                                    case "esmond": 
                                        maurl = service.addresses[service.addresses.length-1]; //grab the last one
                                        break;
                                    default:
                                        running.push(service.name);
                                    }
                                }
                            });
                            //console.log("maurl:"+maurl);
                            //console.dir(running);

                            //construct ma urls
                            running.forEach(function(service) {
                                switch(service) {
                                case "bwctl":
                                    //andy lake says write_url is currrently not used
                                    mas.push({ read_url: maurl, /*write_url: maurl,*/ type: 'perfsonarbuoy/bwctl'});
                                    break;
                                case "owamp":
                                    mas.push({ read_url: maurl, /*write_url: maurl,*/ type: 'perfsonarbuoy/owamp'});
                                    break;
                                case "traceroute":
                                    mas.push({ read_url: maurl, /*write_url: maurl,*/ type: 'traceroute'});
                                    break;
                                case "pinger": //TODO right service name?
                                    mas.push({ read_url: maurl, /*write_url: maurl,*/ type: 'pinger'});
                                    break;
                                
                                //don't know what to do with these services
                                case "ndt":
                                case "regular_testing":
                                    break;
                                }
                            });
                        } catch(SyntaxError) {
                            //syntax error probably means this is 3.3 site returning html
                        }
                    }
                    next();
                });
            }).on('error', function(e) {
                //ignore
                //console.log("failed to access:"+url);
                next();
            });
        }
    ], function(err) {
        cb(null, mas); 
    });
}


