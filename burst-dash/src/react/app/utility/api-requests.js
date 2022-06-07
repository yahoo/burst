export const baseURL = typeof document !== 'undefined' ? new URL(document.URL).host : 'BASE_URL';
// export const baseURL = 'burst-canary.flurry.com:37030';
// export const baseURL = 'burst.flurry.com:37030';

export const maxFetchSize = 100;

export const FormMimeType = 'application/x-www-form-urlencoded';
export const JsonMimeType = 'application/json';

export const FormMimeTypeHeader = {'Content-Type': FormMimeType};
export const JsonMimeTypeHeader = {'Content-Type': JsonMimeType};

export function formParameters(parameters) {
    return Object.entries(parameters)
        .map(([key, val]) => `${encodeURIComponent(key)}=${encodeURIComponent(val)}`)
        .join("&");
}

export const asJson = (parameters) => ({parameters, headers: JsonMimeTypeHeader, encoder: JSON.stringify});

// compatibility hack for using Rewire in unit tests
const httpRequest = typeof fetch !== 'undefined' && fetch;
export default function request(endpoint, {
    parameters = {},
    method = 'POST',
    headers = FormMimeTypeHeader,
    encoder = formParameters,
} = {}) {
    const qs = Object.keys(parameters).reduce((params, name) => {
        const val = parameters[name];
        const key = encodeURIComponent(name);
        if (val instanceof Array) {
            params = params.concat(val.map(v => `${key}=${encodeURIComponent(v)}`))
        } else {
            params.push(`${key}=${encodeURIComponent(val)}`);
        }
        return params;
    }, []).join('&');
    return httpRequest(`//${baseURL}/api/master${endpoint}${method !== 'POST' && qs ? `?${qs}` : ''}`, {
        method,
        headers,
        credentials: 'include',
        body: method === 'POST' ? encoder(parameters) : undefined
    }).then(
        response => response.json(),
        error => {
            throw error
        }
    ).then(
        json => {
            if (json.hasOwnProperty('success') && !json.success && json.hasOwnProperty('error')) {
                throw new Error(json.error);
            }
            return json;
        }
    )
}
