import expect from 'expect.js';

export function expectInitialState(state = {}, initialState = {}, except = {}) {
    expect(state).to.eql(Object.assign({}, initialState, except));
}

export function expectDispatches(startState, reducer, stateDeltas = []) {
    let call = 0;
    let state = startState;
    return function dispatch(action) {
        call += 1;
        if (call > stateDeltas.length) {
            throw new Error(`Stub called too many times (${call} times, current action: ${action.type})`);
        }
        if (typeof action === 'function') {
            return action(dispatch);
        }
        state = reducer(state, action);
        expectInitialState(state, startState, Object.assign({}, ...stateDeltas.slice(0, call)));
    };
}

export function expectRequest(invocation, url, {mimeType = 'application/x-www-form-urlencoded', method = 'POST'} = {}) {
    const [reqUrl, reqArgs = {}] = invocation.args;
    expect(reqUrl).to.be(url);
    if (method !== 'POST' || reqArgs.method) {
        expect(reqArgs).to.have.property('method', method);
    }
    if (mimeType !== 'application/x-www-form-urlencoded' || reqArgs.headers) {
        expect(reqArgs.headers).to.have.property('Content-Type', mimeType);
    }
}
