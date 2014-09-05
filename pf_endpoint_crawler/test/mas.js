var xml2js = require('xml2js');
var chai = require('chai');
var should = chai.should();
var expect = chai.expect;

var crawler = require('../crawler');

describe('mas', function() {
    it('should download mas from perfsonar-bw.grid.iu.edu', function(done) {
        crawler.find_mas('perfsonar-bw.grid.iu.edu',function(err, mas) {        
            if(err) return done(err);
            var correct = [ { read_url: 'http://perfsonar-bw.grid.iu.edu:8085/perfSONAR_PS/services/pSB',
                    write_url: 'perfsonar-bw.grid.iu.edu:8570',
                    type: 'perfsonarbuoy/bwctl' } 
            ];
            expect(mas).to.deep.equal(correct);
            done();
        });
    });
    it('should download mas from perfsonar-lt.grid.iu.edu', function(done) {
        crawler.find_mas('perfsonar-lt.grid.iu.edu',function(err, mas) {        
            if(err) return done(err);
            expect(mas).to.deep.equal(
                [ { read_url: 'http://perfsonar-lt.grid.iu.edu:8086/perfSONAR_PS/services/tracerouteMA',
                write_url: 'http://perfsonar-lt.grid.iu.edu:8086/perfSONAR_PS/services/tracerouteCollector',
                type: 'traceroute' },
                { read_url: 'http://perfsonar-lt.grid.iu.edu:8085/perfSONAR_PS/services/pSB',
                write_url: 'perfsonar-lt.grid.iu.edu:8569',
                type: 'perfsonarbuoy/owamp' } ]);
            done();
        });
    });
});
