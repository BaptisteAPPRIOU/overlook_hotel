/**
 * Sends a POST request to the "/api/v1/register" endpoint to register a new user.
 *
 * @param {string} firstName - The user's first name.
 * @param {string} lastName - The user's last name.
 * @param {string} fullEmail - The user's email address.
 * @param {string} password - The user's password.
 * @returns {Promise<Response>} The fetch API Response object from the registration request.
 */

document
  .getElementById("registerForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const emailPrefix = document.getElementById("emailPrefix").value.trim();
    const password = document.getElementById("password").value.trim();
    const messageElem = document.getElementById("message");

    const fullEmail = `${emailPrefix}@olh.fr`;
    document.getElementById("email").value = fullEmail;

    showMessage(messageElem, "");

    try {
      const response = await fetch("/api/v1/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          firstName,
          lastName,
          email: fullEmail,
          password,
        }),
      });

      if (!response.ok) {
        showMessage(
          messageElem,
          await getApiErrorMessage(
            response,
            "Registration failed. Please try again.",
          ),
          "error",
        );
        return;
      }

      showMessage(
        messageElem,
        "Registration successful! Redirecting...",
        "success",
      );

      setTimeout(() => {
        window.location.href = "/clientLogin";
      }, 2000);
    } catch (error) {
      showMessage(messageElem, "Network error. Please try again.", "error");
    }
  });

async function getApiErrorMessage(response, fallback) {
  try {
    const error = await response.json();
    const fieldMessages = Object.entries(error.errors || {}).map(
      ([field, message]) => `${formatFieldName(field)}: ${message}`,
    );

    return fieldMessages.length > 0
      ? fieldMessages.join(" ")
      : error.message || fallback;
  } catch {
    return fallback;
  }
}

function formatFieldName(field) {
  return field.replace(/([A-Z])/g, " $1").replace(/^./, (letter) =>
    letter.toUpperCase(),
  );
}

function showMessage(element, text, type) {
  element.textContent = text;
  element.classList.toggle("error", type === "error");
  element.classList.toggle("success", type === "success");
}
