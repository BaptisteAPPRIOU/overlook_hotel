/**
 * Sends a POST request to the "/api/v1/login" endpoint with the provided email and password.
 * 
 * @constant
 * @type {Response}
 * @async
 * @param {string} email - The user's email address.
 * @param {string} password - The user's password.
 * @returns {Promise<Response>} The fetch API Response object from the login request.
 */

document.getElementById("employeeLoginForm").addEventListener("submit", async function (e) {
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
    const { token, role } = data;

    if (!token) {
      messageElem.textContent = "Token not received.";
      return;
    }

    localStorage.setItem("jwtToken", token);
    
    // Also set as cookie for server-side pages (remove secure flag for localhost)
    document.cookie = `jwtToken=${token}; path=/; samesite=strict`;

    if (role === "EMPLOYEE" || role === "ADMIN") {
      window.location.href = "/employeeDashboard";
      }
    else if (role === "CLIENT"){
        window.location.href = "/clientDashboard";
    } else {
      messageElem.textContent = "Unauthorized role for this page.";
    }
  } catch (error) {
    messageElem.textContent = "Network error, please try again.";
  }
});
