var mysql = require('mysql');
var config = require('./config.json');
var con = mysql.createConnection(config.oim_dburl);
var sids = config.oim_service_ids.join(",");

exports.list_wlcg_endpoints = function(cb) {
    var hostnames = [];
    con.query("SELECT hostname FROM wlcg_endpoint WHERE service_id IN ("+sids+")", function(err, rows) {
        if(err) return cb(err);
        rows.forEach(function(row) {
            hostnames.push(row.hostname);
        });
        cb(null, hostnames);
    });
}

exports.list_oim_endpoints = function(cb) {
    //first load overrides
    con.query("SELECT * from resource_service_detail WHERE `key` = 'endpoint' "+       
        "AND service_id IN ("+sids+")", function(err, overrides) {
        if(err) return cb(err);

        //then load resource/service
        var fqdns = [];
        con.query("SELECT id, fqdn, rs.service_id FROM resource JOIN resource_service rs ON rs.resource_id = id "+
            "WHERE rs.service_id IN ("+sids+") and active = 1 and disable = 0", function(err, rows){
            if(err) return cb(err);
    
            rows.forEach(function(row) {
                var fqdn = row.fqdn;

                //find overrides
                overrides.forEach(function(o) {
                    if(o.resource_id == row.id && o.service_id == row.service_id) {
                        //console.log("found override for");
                        //console.dir(row);
                        //console.dir(o);
                        fqdn = o.value;
                    } 
                });
                fqdns.push(fqdn);
            });
            cb(null, fqdns);
        });
    });
}

exports.upsertMAs = function(hostname, mas, cb) {
    var mas_json = JSON.stringify(mas);
    con.query("SELECT * FROM perfsonar_mas WHERE hostname = '"+hostname+"'", function(err, rows) {
        if(err) throw err;
        if(rows.length == 0) {
            console.log("inserting new ma "+hostname);
            con.query("INSERT INTO perfsonar_mas (hostname, ma) VALUES ("+ con.escape(hostname)+", "+ con.escape(mas_json)+ ")", cb);
        } else {
            console.log("updating "+hostname);
            con.query("UPDATE perfsonar_mas SET ma = "+con.escape(mas_json)+" WHERE hostname = "+con.escape(hostname), cb);
        }
    });
}

