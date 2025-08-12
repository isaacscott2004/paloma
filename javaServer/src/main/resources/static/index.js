// Global State
let authToken = '';



// Utility Functions
function scrollToId(id) {
  const element = document.getElementById(id);
  if (element) {
    window.scrollBy({
      top: element.getBoundingClientRect().top,
      behavior: "smooth"
    });
  }
}

function setAuthToken(token) {
  authToken = token;
  document.getElementById('authToken').value = token;
}

function storeResponseTokens(data) {
  if (data.accessAuthToken || data.token) {
    setAuthToken("Bearer " + (data.accessAuthToken || data.token));
  } else if (data.authToken) {
    setAuthToken(data.authToken);
  }

  if (data.refreshAuthToken) {
    localStorage.setItem('refreshToken', data.refreshAuthToken);
  }

  const userId = data.user?.id || data.userId;
  if (userId) {
    localStorage.setItem('userId', userId);
  }


}

// HTTP Logic
function submit() {
  const method = document.getElementById('method').value;
  const endpoint = document.getElementById('handleBox').value;
  const requestBody = document.getElementById('requestBox').value;
  authToken = document.getElementById('authToken').value;

  if (!endpoint || !method) return false;

  send(endpoint, requestBody, method);
  return false;
}

function send(path, body, method) {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': authToken
  };

  fetch(path, { method, headers, body: body || undefined })
      .then(response => {
        const statusLine = `${response.status}: ${response.statusText}`;
        const contentType = response.headers.get('content-type');
        return response.text().then(text => ({ text, contentType, statusLine }));
      })
      .then(({ text, contentType, statusLine }) => {
        let parsed;
        try {
          parsed = contentType.includes('application/json') ? JSON.parse(text) : { nonJsonResponse: text };
        } catch {
          parsed = { error: 'Invalid JSON response', originalText: text };
        }

        if (parsed.nonJsonResponse) {
          showResponse(statusLine, `Non-JSON Response:\n${parsed.nonJsonResponse.slice(0, 500)}`);
        } else if (parsed.error) {
          showResponse(statusLine, `Error: ${parsed.error}\n\nOriginal Text:\n${parsed.originalText?.slice(0, 500)}`);
        } else {
          storeResponseTokens(parsed);
          const responseText = JSON.stringify(parsed, null, 2) || 'Empty response body';
          showResponse(statusLine, responseText);
        }
      })
      .catch(err => {
        const message = buildErrorMessage(err.message);
        showResponse('', `Error: ${err.message}\n\n${message}`);
      });
}

function showResponse(status, text) {
  document.getElementById('response').innerText = `${status}\n${text}`;
  scrollToId('responseBox');
}

function buildErrorMessage(message) {
  if (message.includes('Failed to fetch')) {
    return 'This could be due to:\n- Network issues\n- CORS errors\n- Server not running';
  }
  if (message.includes('JSON')) {
    return 'Possible reasons:\n- Non-JSON server response\n- Authentication redirect\n- HTML error page';
  }
  return '';
}

// Request Templates
function displayRequest(method, endpoint, request = null) {
  document.getElementById('method').value = method;
  document.getElementById('handleBox').value = endpoint;
  document.getElementById('requestBox').value = request ? JSON.stringify(request, null, 2) : '';
  scrollToId('execute');
}

// User Actions
function clearAll() {
  displayRequest('DELETE', '/db');
}
function register() {
  displayRequest('POST', '/auth/register', {
    username: 'testuser',
    password: 'password123',
    email: 'test@example.com',
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
  displayRequest('POST', '/insession/logout');
}
function refresh() {
  displayRequest('POST', '/insession/refresh');
}


// Check-in & Medications
function dailyCheckIn() {
  displayRequest('POST', '/insession/dailyCheckin', {
    moodScore: 8,
    energyScore: 7,
    motivationScore: 6,
    suicidalScore: 2,
    notes: "Feeling pretty good today"
  });
}
function getScore() {
  displayRequest('GET', '/loggedIn');
}
function addMedication() {
  displayRequest('POST', '/loggedIn', { medication: 'medication', amount: 'amount' });
}
function updateMedication() {
  displayRequest('PUT', '/loggedIn', { medication: 'medication', amount: 'amount' });
}
function medications() {
  displayRequest('POST', '/loggedIn', { medications: 'medications', amount: 'amount' });
}
function getMedications() {
  displayRequest('GET', '/loggedIn');
}
function addContact() {
  displayRequest('POST', '/insession/add/contact', { 
    email: 'isaacscottirwin@gmail.com',
    messageOnNotify: 'Please help me if I need support' 
  });
}
function removeContact() {
  displayRequest('DELETE', '/insession/remove/contact', { 
    email: 'isaacscottirwin@gmail.com'
  });
}
function addAlertSensitivity() {
  displayRequest('POST', '/insession/add/sensitivityLevel', {
    sensitivityLevel: 'MEDIUM'
  });
}
function updateAlertSensitivity() {
  displayRequest('PUT', '/insession/update/sensitivityLevel', {
    sensitivityLevel: 'HIGH'
  });
}
function updateEmail() {
  displayRequest('PUT', '/insession/update/email', { newEmail: 'newemail@example.com' });
}
function updatePassword() {
  displayRequest('PUT', '/insession/update/password', { 
    oldPassword: 'currentPassword', 
    newPassword: 'newPassword' 
  });
}
function updateUsername() {
  displayRequest('PUT', '/insession/update/username', { newUsername: 'newUsername' });
}
function getContacts() {
  displayRequest('GET', '/loggedIn');
}
