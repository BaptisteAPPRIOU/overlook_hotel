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

    const token = await response.text();
    // localStorage.setItem("token", token);

    // Redirect to employee dashboard page
    window.location.href = "/employeeDashboard.html"; // à adapter
  } catch (error) {
    messageElem.textContent = "Network error, please try again.";
  }
});
