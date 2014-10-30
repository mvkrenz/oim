var xml2js = require('xml2js');
var chai = require('chai');
var should = chai.should();
var expect = chai.expect;

var crawler = require('../crawler');

describe('crawler/MA endpoint', function() {
    it('should download from random 3.3 site', function(done) {
        this.timeout(10000);
        crawler.find_mas('lhcmon.bnl.gov',function(err, mas) {        
            if(err) return done(err);
            var correct = [ 
                { read_url: 'http://lhcmon.bnl.gov:8085/perfSONAR_PS/services/pSB',
                write_url: 'lhcmon.bnl.gov:8570',
                type: 'perfsonarbuoy/bwctl' } 
            ]
            expect(mas).to.deep.equal(correct);
            done();
        });
    });

    it('should download mas from perfsonar-bw.grid.iu.edu', function(done) {
        this.timeout(10000);
        crawler.find_mas('perfsonar-bw.grid.iu.edu',function(err, mas) {        
            if(err) return done(err);
            /* 3.3
            var correct = [ { read_url: 'http://perfsonar-bw.grid.iu.edu:8085/perfSONAR_PS/services/pSB',
                    write_url: 'perfsonar-bw.grid.iu.edu:8570',
                    type: 'perfsonarbuoy/bwctl' } 
            ];
            */
            var correct = [ 
                { read_url: 'http://perfsonar-bw.grid.iu.edu/esmond/perfsonar/archive/',
                write_url: 'http://perfsonar-bw.grid.iu.edu/esmond/perfsonar/archive/',
                type: 'perfsonarbuoy/bwctl' } 
            ];
            expect(mas).to.deep.equal(correct);
            done();
        });
    });
    it('should download mas from perfsonar-lt.grid.iu.edu', function(done) {
        this.timeout(10000);
        crawler.find_mas('perfsonar-lt.grid.iu.edu',function(err, mas) {        
            if(err) return done(err);
            var correct = [ 
                { read_url: 'http://perfsonar-lt.grid.iu.edu/esmond/perfsonar/archive/',
                write_url: 'http://perfsonar-lt.grid.iu.edu/esmond/perfsonar/archive/',
                type: 'perfsonarbuoy/owamp' }
            ];
            expect(mas).to.deep.equal(correct);
            done();
        });
    });
});


