document.getElementById("clientLoginForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();
  const messageElem = document.getElementById("message");

  messageElem.textContent = ""; // Clear previous messages

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

    const token = await response.text();
    // Save token in localStorage/sessionStorage or cookie if needed
    // localStorage.setItem("token", token);

    // Redirect to client dashboard page
    window.location.href = "/clientDashboard.html"; // à adapter selon ton front
  } catch (error) {
    messageElem.textContent = "Network error, please try again.";
  }
});
