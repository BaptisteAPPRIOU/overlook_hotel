/**
 * Sends a POST request to the "/api/v1/register" endpoint to register a new user.
 *
 * @param {string} firstName - The user's first name.
 * @param {string} lastName - The user's last name.
 * @param {string} fullEmail - The user's email address.
 * @param {string} password - The user's password.
 * @returns {Promise<Response>} The fetch API Response object from the registration request.
 */

document.getElementById("registerForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const firstName = document.getElementById("firstName").value.trim();
  const lastName = document.getElementById("lastName").value.trim();
  const emailPrefix = document.getElementById("emailPrefix").value.trim();
  const password = document.getElementById("password").value.trim();
  const messageElem = document.getElementById("message");

  const fullEmail = `${emailPrefix}@olh.fr`;
  document.getElementById("email").value = fullEmail;

  messageElem.textContent = "";

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
        password
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      messageElem.style.color = "red";
      messageElem.textContent = errorText || "Registration failed";
      return;
    }

    messageElem.style.color = "white";
    messageElem.textContent = "Registration successful! Redirecting...";

    setTimeout(() => {
      window.location.href = "/clientLogin";
    }, 2000);
  } catch (error) {
    messageElem.style.color = "red";
    messageElem.textContent = "Network error, please try again.";
  }
});
