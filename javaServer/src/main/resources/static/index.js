//common functionality
let authToken = '';
let gameID = 0;

function scrollToId(id) {
  window.scrollBy({
    top: document.getElementById(id).getBoundingClientRect().top,
    behavior:"smooth"
  });
}


//HTTP
function submit() {
  document.getElementById('response').value = '';
  const method = document.getElementById('method').value;
  const endpoint = document.getElementById('handleBox').value;
  const requestBody = document.getElementById('requestBox').value;
  authToken = document.getElementById('authToken').value;

  if (endpoint && method) {
    send(endpoint, requestBody, method);
  }

  try {
    const requestObj = JSON.parse(requestBody);
    gameID = requestObj.gameID || gameID;
  } catch (ignored) {}

  return false;
}

function send(path, params, method) {
  params = !!params ? params : undefined;
  let status = '';
  fetch(path, {
    method: method,
    body: params,
    headers: {
      Authorization: authToken,
      'Content-Type': 'application/json',
    },
  })
    .then((response) => {
      status = response.status + ': ' + response.statusText + '\n';
      return response.text();
    })
    .then((text) => {
      if(text) return JSON.parse(text);
      else return text;
    })
    .then((data) => {
      if(data) {
        document.getElementById('authToken').value = authToken = data.authToken || authToken;
        gameID = data.gameID || gameID;
      }
      const response = (data === "") ? "Empty response body" : JSON.stringify(data, null, 2);
      document.getElementById('response').innerText = status + "\n" + response;
      scrollToId('responseBox');
    })
    .catch((error) => {
      document.getElementById('response').innerText = error;
    });
}

function displayRequest(method, endpoint, request) {
  document.getElementById('method').value = method;
  document.getElementById('handleBox').value = endpoint;
  const body = request ? JSON.stringify(request, null, 2) : '';
  document.getElementById('requestBox').value = body;
  scrollToId('execute');
}

function clearAll() {
  displayRequest('DELETE', '/db', null);
}
function register() {
  displayRequest('POST', '/user', { username: 'username', password: 'password', email: 'email' });
}
function login() {
  displayRequest('POST', '/session', { username: 'username', password: 'password' });
}
function logout() {
  displayRequest('DELETE', '/session', null);
}
function dailyCheckIn() {
  displayRequest('POST', '/loggedIn', {scoreOne: 'scoreOne', scoreTwo: 'scoreTwo',
    scoreThree: 'scoreThree', scoreFour: 'scoreFour'});
}
function getScore() {
  displayRequest('GET', '/loggedIn', null);
}
function addMedication() {
  displayRequest('POST', '/loggedIn', {medication: 'medication', amount: 'amount'});
}
function updateMedication() {
  displayRequest('PUT', '/loggedIn', {medication: 'medication', amount: 'amount'});
}
function medications() {
  displayRequest('POST', '/loggedIn', {medications: 'medications', amount: 'amount'});
}
function getMedications() {
  displayRequest('GET', '/loggedIn', null);
}
function addContact() {
  displayRequest('POST', '/loggedIn', {name: 'name', phone: 'phone', email: 'email'});
}
function updateContact() {
  displayRequest('PUT', '/loggedIn', {name: 'name', phone: 'phone', email: 'email'});
}
function getContacts() {
  displayRequest('GET', '/loggedIn', null);
}
//End HTTP




