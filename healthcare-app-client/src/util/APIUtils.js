import { API, AUTH_TOKEN } from '../constants';

const request = (options) => {
    const headers = new Headers({
        'Content-Type': 'application/json',
    })

    if(localStorage.getItem(AUTH_TOKEN)) {
        headers.append('SessionId', localStorage.getItem(AUTH_TOKEN))
    }

    const defaults = {headers: headers};
    const cred = {credentials: 'include'};
    options = Object.assign({}, defaults, options, cred);

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

const create = (options) => {
    const headers = new Headers({
    })

    if(localStorage.getItem(AUTH_TOKEN)) {
        headers.append('SessionId', localStorage.getItem(AUTH_TOKEN))
    }

    const defaults = {headers: headers};
    const cred = {credentials: 'include'};
    options = Object.assign({}, defaults, options, cred);

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

const requestFile = (options) => {
    const headers = new Headers({
        'Content-Type': 'text/plain',
    })

    if(localStorage.getItem(AUTH_TOKEN)) {
        headers.append('SessionId', localStorage.getItem(AUTH_TOKEN))
    }

    const defaults = {headers: headers};
    const cred = {credentials: 'include'};
    options = Object.assign({}, defaults, options, cred);

    return fetch(options.url, options)
    .then(response =>
      response.arrayBuffer().then((buffer) => {
      var txtStr = arrayBufferToBase64(buffer);

      return txtStr;
      })
    );
};

const requestImg = (options) => {
    const headers = new Headers({
        'Content-Type': 'image',
    })

    if(localStorage.getItem(AUTH_TOKEN)) {
        headers.append('SessionId', localStorage.getItem(AUTH_TOKEN))
    }

    const defaults = {headers: headers};
    const cred = {credentials: 'include'};
    options = Object.assign({}, defaults, options, cred);

    return fetch(options.url, options)
    .then(response =>
        response.arrayBuffer().then((buffer) => {
        var base64Flag = 'data:image;base64,';
        var imageStr = arrayBufferToBase64(buffer);

        return base64Flag + imageStr;
        })
    );
};

const requestVideo = (options) => {
    const headers = new Headers({
        'Content-Type': 'video/mp4',
    })

    if(localStorage.getItem(AUTH_TOKEN)) {
        headers.append('SessionId', localStorage.getItem(AUTH_TOKEN))
    }

    const defaults = {headers: headers};
    const cred = {credentials: 'include'};
    options = Object.assign({}, defaults, options, cred);

    return fetch(options.url, options)
    .then(response =>
        response.arrayBuffer().then((buffer) => {
    var videoStr = buffer;

    return videoStr;
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
        url: API + "/auth/signin",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function getServerSignature(loginRequest) {
    return request({
        url: API + "/auth/firstAuthorization",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function verifyTagSignature(loginRequest) {
    return request({
        url: API + "/auth/secondAuthorization",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function signup(signupRequest) {
    return request({
        url: API + "/auth/signup",
        method: 'POST',
        body: JSON.stringify(signupRequest)
    });
}

export function logout() {
    return request({
        url: API + "/user/logout",
        method: 'GET',
    });
}

export function getCurrentUser() {
    if(!localStorage.getItem(AUTH_TOKEN)) {
        return Promise.reject("No access token set.");
    }

    return request({
        url: API + "/user/me",
        method: 'GET'
    });
}

export function getUserProfile(nric) {
    return request({
        url: API + "/users/" + nric,
        method: 'GET'
    });
}

export function createRecord(newRecord, file) {
    const formData = new FormData();
    formData.append("recordRequest", JSON.stringify(newRecord))
    formData.append("file", file, file.name)
    return create({
        url: API + "/records/create/",
        method: 'POST',
        body: formData
    });
}

export function getAllRecords() {
    return request({
        url: API + "/records/",
        method: 'GET'
    });
}

export function getUserRecords(nric, role) {
    return request({
        url: API + "/records/" + role + "/" + nric,
        method: 'GET'
    });
}

export function getAllUsers() {
    return request({
        url: API + "/admin/showAllUsers",
        method: 'GET'
    });
}

export function deleteUser(nric) {
    return request({
        url: API + "/admin/delete/" + nric,
        method: 'GET'
    });
}

export function downloadFile(filename) {
    return requestFile({
        url: API + "/file/download/" + filename,
        method: 'GET'
    });
}

export function downloadVideo(filename) {
    return requestVideo({
        url: API + "/file/download/" + filename,
        method: 'GET'
    });
}

export function downloadImg(filename) {
    return requestImg({
        url: API + "/file/download/" + filename,
        method: 'GET'
    });
}

export function assign(nrics) {
    return request({
        url: API + "/treatments/start/",
        method: 'POST',
        body: JSON.stringify(nrics)
    });
}

export function unassign(nrics) {
    return request({
        url: API + "/treatments/stop/",
        method: 'POST',
        body: JSON.stringify(nrics)
    });
}
