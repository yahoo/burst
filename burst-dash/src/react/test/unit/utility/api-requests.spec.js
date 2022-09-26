import expect from 'expect.js';
import sinon from 'sinon';
import request, {
    __RewireAPI__ as RewireAPI,
    asJson,
    baseURL,
    formParameters,
    JsonMimeTypeHeader
} from '../../../app/utility/api-requests';

const fetch = sinon.stub().named('fetch-stub')
RewireAPI.__Rewire__('httpRequest', fetch);

class MockResponse {
    json() {
        return Promise.resolve({});
    }
}

describe('API Requests', function () {

    beforeEach(function () {
        fetch.reset();
        fetch.resolves(new MockResponse());
    });

    describe('baseURL', function () {
        it('should correctly compute the base url', function () {
            expect(baseURL).to.equal('BASE_URL')
        })
    });

    describe('request', function () {
        it('should append the base path to requests', function () {
            request('/an/endpoint');
            expect(fetch.callCount).to.equal(1);
            expect(fetch.firstCall.args[0]).to.equal('//BASE_URL/api/supervisor/an/endpoint');
        });

        it('should default to POST', function () {
            request('/an/endpoint');
            expect(fetch.firstCall.args[1]).to.have.property('method', 'POST');
        });

        it('should allow overriding the HTTP method', function () {
            request('/an/endpoint', {method: 'GET'});
            expect(fetch.firstCall.args[1]).to.have.property('method', 'GET');
        });

        it('should encode parameters as form body by default', function () {
            request('/an/endpoint', {parameters: {pk: 1}});
            expect(fetch.firstCall.args[1]).to.have.property('body', 'pk=1');
        });

        it('should send query parameters instead of a body on GET requests', function () {
            request('/an/endpoint', {
                method: 'GET',
                parameters: {
                    'a number': 1,
                    foo: 'bar/baz',
                    many: [1, 2, 3]
                }
            });
            expect(fetch.firstCall.args[0]).to.equal('//BASE_URL/api/supervisor/an/endpoint?a%20number=1&foo=bar%2Fbaz&many=1&many=2&many=3');
        });
    });

    describe('encoders', function () {
        it('formParamters should encode data', function () {
            expect(formParameters({a: 1, b: 2})).to.equal('a=1&b=2');
            expect(formParameters({a: 'foo/bar', 'hello &': 'stuff'})).to.equal('a=foo%2Fbar&hello%20%26=stuff')
        });

        it('asJson should stringify the object', function () {
            expect(asJson({pk: 1})).to.eql({
                parameters: {pk: 1},
                headers: JsonMimeTypeHeader,
                encoder: JSON.stringify
            });
        });
    });
});
