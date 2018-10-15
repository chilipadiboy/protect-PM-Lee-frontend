import { API_BASE_URL, AUTH_TOKEN } from '../constants';

const request = (options) => {
    const headers = new Headers({
        'Content-Type': 'application/json',
    })

    if(localStorage.getItem(AUTH_TOKEN)) {
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(AUTH_TOKEN))
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);

    return fetch(options.url, options)
    .then(response =>
        response.json().then(json => {
            if(!response.ok) {
                return Promise.reject(json);
            }
            return json;
        })
    );
};

const download = (options) => {
    const headers = new Headers({
        'Content-Type': 'image',
    })

    if(localStorage.getItem(AUTH_TOKEN)) {
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(AUTH_TOKEN))
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);

    return fetch(options.url, options)
    .then(response =>
        response.arrayBuffer().then((buffer) => {
    var base64Flag = 'data:image;base64,';
    var imageStr = arrayBufferToBase64(buffer);

    return base64Flag + imageStr;
        })
    );
};

function arrayBufferToBase64(buffer) {
  var binary = '';
  var bytes = [].slice.call(new Uint8Array(buffer));

  bytes.forEach((b) => binary += String.fromCharCode(b));

  return window.btoa(binary);
};

export function login(loginRequest) {
    return request({
        url: API_BASE_URL + "/auth/signin",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function loginWithTag(loginRequest) {
    return request({
        url: API_BASE_URL + "/auth/signinWithTag",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function signup(signupRequest) {
    return request({
        url: API_BASE_URL + "/auth/signup",
        method: 'POST',
        body: JSON.stringify(signupRequest)
    });
}

export function getCurrentUser() {
    if(!localStorage.getItem(AUTH_TOKEN)) {
        return Promise.reject("No access token set.");
    }

    return request({
        url: API_BASE_URL + "/user/me",
        method: 'GET'
    });
}

export function getUserProfile(nric) {
    return request({
        url: API_BASE_URL + "/users/" + nric,
        method: 'GET'
    });
}

export function createRecord(newRecord) {
    return request({
        url: API_BASE_URL + "/records/",
        method: 'POST',
        body: JSON.stringify(newRecord)
    });
}

export function getAllRecords() {
    return request({
        url: API_BASE_URL + "/records/",
        method: 'GET'
    });
}

export function getUserRecords(nric, role) {
    return request({
        url: API_BASE_URL + "/records/" + role + "/" + nric,
        method: 'GET'
    });
}

export function getAllUsers() {
    return request({
        url: API_BASE_URL + "/admin/showAllUsers",
        method: 'GET'
    });
}

export function deleteUser(nric) {
    return request({
        url: API_BASE_URL + "/admin/delete/" + nric,
        method: 'GET'
    });
}

export function downloadFile(filename) {
    return request({
        url: API_BASE_URL + "/file/download/" + filename,
        method: 'GET'
    });
}

export function downloadImg(filename) {
    return download({
        url: API_BASE_URL + "/file/download/" + filename,
        method: 'GET'
    });
}
