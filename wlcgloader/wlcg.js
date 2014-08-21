
var https = require('https');
var fs = require('fs');
var config = require('./config.json');

//method = get_site or get_service_endpoint
exports.load_xml = function(method, path, cb) {
    var options = {
        port: 443,
        key: fs.readFileSync(config.userkey),
        cert: fs.readFileSync(config.usercert),
        ca: fs.readFileSync(config.gocdb_ca),
        hostname: config.gocdb_host,
        path: config.gocdb_url+'?method='+method
    };

    var data = "";

    https.get(options, function(res) {
        if(res.statusCode != 200) {
            console.dir(res);
            return cb("non 200 return code");
        }
        res.on('data', function(d) {
            data += d;
        });
        res.on('end', function() {
            //console.log("Storing cache to:"+path);
            fs.writeFileSync(path, data);
            cb(null, data);
        });
    }).on('error', function(e) {
        cb(e);
    });
}

