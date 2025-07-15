/**
 * Sends a POST request to the "/api/v1/login" endpoint with the provided email and password.
 * 
 * @constant
 * @type {Response}
 * @async
 * @summary Fetches the login response from the server.
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/API/Response|Response}
 */

document.getElementById("clientLoginForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();
  const messageElem = document.getElementById("message");

  messageElem.textContent = "";

  try {
    const response = await fetch("/api/v1/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      messageElem.textContent = errorText || "Login failed";
      return;
    }

    const data = await response.json();
    const token = data.token;

    if (!token) {
      messageElem.textContent = "Token not received.";
      return;
    }

    localStorage.setItem("jwtToken", token);
    
    // Also set as cookie for server-side pages (remove secure flag for localhost)
    document.cookie = `jwtToken=${token}; path=/; samesite=strict`;

    window.location.href = "/clientDashboard.html";
  } catch (error) {
    messageElem.textContent = "Network error, please try again.";
  }
});
