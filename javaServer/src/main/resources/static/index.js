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
        // Handle JWT authentication responses
        if (data.accessAuthToken) {
          document.getElementById('authToken').value = authToken = "Bearer " + data.accessAuthToken;
        } else if (data.token) {
          document.getElementById('authToken').value = authToken = "Bearer " + data.token;
        } else {
          document.getElementById('authToken').value = authToken = data.authToken || authToken;
        }
        
        // Store refresh token for later use
        if (data.refreshAuthToken) {
          localStorage.setItem('refreshToken', data.refreshAuthToken);
        }
        
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
  displayRequest('POST', '/auth/register', { 
    username: 'testuser', 
    password: 'password123', 
    email: 'test@example.com',
    phone: '+1234567890',
    fullName: 'Test User',
    roleType: 'USER'
  });
}
function login() {
  displayRequest('POST', '/auth/login', { 
    emailOrUsername: 'testuser', 
    password: 'password123' 
  });
}
function logout() {
  displayRequest('POST', '/auth/logout', { user: { id: 'user-id-here' } });
}
function refresh() {
  const storedRefreshToken = localStorage.getItem('refreshToken');
  displayRequest('POST', '/auth/refresh', { 
    refreshToken: storedRefreshToken || 'refresh-token-here' 
  });
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




