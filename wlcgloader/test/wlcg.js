var xml2js = require('xml2js');
var chai = require('chai');
var should = chai.should();
var expect = chai.expect;

var wlcg = require('../wlcg');

describe('wlcg', function() {
    var xml;
    var data;
    it('should download site', function(done) {
        this.timeout(1000*5); //give 5 seconds to load
        wlcg.load_xml('get_site', '/dev/null', function(err, _xml) {        
            if(err) return done(err);
            xml = _xml;
            done();
        });
    });

    it('should parse xml', function(done) {
        var parser = new xml2js.Parser();
        parser.parseString(xml, function(err, _data) {
            if(err) return done(err);
            data = _data;
            expect(data.results.SITE.length).to.be.above(10);
            done();
        });
    });
});
