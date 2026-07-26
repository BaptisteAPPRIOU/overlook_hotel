/**
 * Sends a POST request to the "/api/v1/login" endpoint with the provided email and password.
 *
 * @constant
 * @type {Response}
 * @async
 * @summary Fetches the login response from the server.
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/API/Response|Response}
 */

document
  .getElementById("clientLoginForm")
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
      const token = data.token;

      if (!token) {
        showMessage(messageElem, "Unable to sign in. Please try again.", "error");
        return;
      }

      localStorage.setItem("jwtToken", token);

      // Also set as cookie for server-side pages (remove secure flag for localhost)
      document.cookie = `jwtToken=${token}; path=/; samesite=strict`;

      window.location.href = "/clientHomePage";
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
