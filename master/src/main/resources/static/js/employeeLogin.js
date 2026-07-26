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

document
  .getElementById("employeeLoginForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const messageElem = document.getElementById("message");

    showMessage(messageElem, "");

    try {
      const response = await fetch("/api/v1/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        showMessage(
          messageElem,
          await getApiErrorMessage(response, "Login failed. Please try again."),
          "error",
        );
        return;
      }

      const data = await response.json();
      const { token, role } = data;

      if (!token) {
        showMessage(messageElem, "Unable to sign in. Please try again.", "error");
        return;
      }

      localStorage.setItem("jwtToken", token);
      localStorage.setItem("userRole", role);

      // Also set as cookie for server-side pages (remove secure flag for localhost)
      document.cookie = `jwtToken=${token}; path=/; samesite=strict`;

      if (
        role === "EMPLOYEE" ||
        role === "RESPONSABLE" ||
        role === "ADMIN"
      ) {
        window.location.href = "/employeeDashboard";
      } else if (role === "CLIENT") {
        window.location.href = "/clientDashboard";
      } else {
        showMessage(messageElem, "Unauthorized role for this page.", "error");
      }
    } catch (error) {
      showMessage(messageElem, "Network error. Please try again.", "error");
    }
  });

async function getApiErrorMessage(response, fallback) {
  try {
    const error = await response.json();
    const fieldMessages = Object.values(error.errors || {});

    return fieldMessages.length > 0
      ? fieldMessages.join(" ")
      : error.message || fallback;
  } catch {
    return fallback;
  }
}

function showMessage(element, text, type) {
  element.textContent = text;
  element.classList.toggle("error", type === "error");
  element.classList.toggle("success", type === "success");
}
