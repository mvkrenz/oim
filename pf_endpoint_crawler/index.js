var https = require('https');
var http = require('http');
var fs = require('fs');

var _ = require('lodash');
var async = require('async');
var xml2js = require('xml2js');
var ps =  require('perfsonar');

var config = require('./config.json');
var oim = require('./oim');
var crawler = require('./crawler');

var all_hostnames = [];
async.series([
    function(done) {
        oim.list_wlcg_endpoints(function(err, hostnames) {
            hostnames.forEach(function(hostname) {
                if(!~all_hostnames.indexOf(hostname)) {
                    all_hostnames.push(hostname);
                }
            });
            done();
        });
    },
    function(done) {
        oim.list_oim_endpoints(function(err, hostnames) {
            hostnames.forEach(function(hostname) {
                if(!~all_hostnames.indexOf(hostname)) {
                    all_hostnames.push(hostname);
                }
            });
            done();
        });
    }
], function(err) {
    //console.dir(all_hostnames);
    async.eachSeries(all_hostnames, function(hostname, done) {
        crawler.find_mas(hostname, function(err, mas) {
            if(err) {
                console.error("errored on "+hostname);
                return done(); //skip to next?
            } else {
                console.dir(mas);
                oim.upsertMAs(hostname, mas, done);
            }
        });
    }, function(err) {
        console.log("all done");
    });
});


