document.getElementById("registerForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const firstName = document.getElementById("firstName").value.trim();
  const lastName = document.getElementById("lastName").value.trim();
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();
  const messageElem = document.getElementById("message");

  messageElem.textContent = "";

  try {
    const response = await fetch("/api/v1/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ firstName, lastName, email, password }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      messageElem.textContent = errorText || "Registration failed";
      return;
    }

    const data = await response.json();
    messageElem.style.color = "green";
    messageElem.textContent = "Registration successful! You can now login.";

    // Optional: redirect to login page after delay
    setTimeout(() => {
      window.location.href = "/html/clientLoginPage.html";
    }, 2000);
  } catch (error) {
    messageElem.textContent = "Network error, please try again.";
  }
});
