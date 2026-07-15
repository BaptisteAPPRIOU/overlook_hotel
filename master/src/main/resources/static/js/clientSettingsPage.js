// Shared user settings dashboard page

let settingsInitialState = null;
let settingsLoaded = false;

document.addEventListener("DOMContentLoaded", function () {
  setupSettingsForm();
  setupPasswordModal();

  if (window.location.hash === "#settings") {
    loadUserSettings();
  }
});

document.addEventListener("dashboard:section-change", function (event) {
  if (event.detail?.sectionId === "settings") {
    loadUserSettings();
  }
});

function setupSettingsForm() {
  const form = document.getElementById("settingsForm");

  if (!form) {
    return;
  }

  form.addEventListener("input", updateSettingsSaveState);
  form.addEventListener("submit", saveUserSettings);
}

function setupPasswordModal() {
  document
    .getElementById("openPasswordModal")
    ?.addEventListener("click", openPasswordSettingsModal);

  document.addEventListener("click", function (event) {
    if (event.target.closest("[data-settings-modal-close]")) {
      closePasswordSettingsModal();
    }
  });

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
      closePasswordSettingsModal();
    }
  });

  document
    .getElementById("passwordSettingsForm")
    ?.addEventListener("submit", saveUserPassword);
}

async function loadUserSettings(forceRefresh = false) {
  if (settingsLoaded && !forceRefresh) {
    return;
  }

  try {
    const user = await fetchCurrentUserSettings();
    settingsLoaded = true;
    fillSettingsForm(user);
    syncHeaderUser(user);
  } catch (error) {
    console.error("Error loading user settings:", error);
    notifySettings("Unable to load settings");
  }
}

async function fetchCurrentUserSettings() {
  const response = await fetch("/api/v1/users/me", {
    method: "GET",
    headers: getSettingsHeaders(),
  });

  if (!response.ok) {
    throw new Error("Failed to load settings");
  }

  return response.json();
}

function fillSettingsForm(user) {
  const firstName = document.getElementById("settingsFirstName");
  const lastName = document.getElementById("settingsLastName");
  const email = document.getElementById("settingsEmail");
  const displayName = document.getElementById("settingsDisplayName");

  if (!firstName || !lastName || !email) {
    return;
  }

  firstName.value = user.firstName || "";
  lastName.value = user.lastName || "";
  email.value = user.email || "";

  settingsInitialState = readSettingsFormState();

  if (displayName) {
    displayName.textContent = getUserDisplayName(settingsInitialState);
  }

  updateSettingsSaveState();
}

function readSettingsFormState() {
  return {
    firstName: document.getElementById("settingsFirstName")?.value.trim() || "",
    lastName: document.getElementById("settingsLastName")?.value.trim() || "",
    email: document.getElementById("settingsEmail")?.value.trim() || "",
  };
}

function updateSettingsSaveState() {
  const saveButton = document.getElementById("settingsSaveButton");

  if (!saveButton || !settingsInitialState) {
    return;
  }

  const currentState = readSettingsFormState();
  const hasChanged =
    currentState.firstName !== settingsInitialState.firstName ||
    currentState.lastName !== settingsInitialState.lastName ||
    currentState.email !== settingsInitialState.email;

  saveButton.disabled = !hasChanged;
}

async function saveUserSettings(event) {
  event.preventDefault();

  const saveButton = document.getElementById("settingsSaveButton");
  const payload = readSettingsFormState();

  if (saveButton?.disabled) {
    return;
  }

  try {
    if (saveButton) {
      saveButton.disabled = true;
    }

    const response = await fetch("/api/v1/users/me", {
      method: "PUT",
      headers: getSettingsHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error("Failed to save settings");
    }

    const user = await response.json();
    updateStoredToken(user.token);
    fillSettingsForm(user);
    syncHeaderUser(user);
    notifySettings("Settings saved");
  } catch (error) {
    console.error("Error saving user settings:", error);
    notifySettings("Unable to save settings");
    updateSettingsSaveState();
  }
}

async function saveUserPassword(event) {
  event.preventDefault();

  const newPassword = document.getElementById("settingsNewPassword")?.value || "";
  const confirmPassword =
    document.getElementById("settingsConfirmPassword")?.value || "";

  if (newPassword !== confirmPassword) {
    notifySettings("Passwords do not match");
    return;
  }

  try {
    const response = await fetch("/api/v1/users/me/password", {
      method: "PUT",
      headers: getSettingsHeaders(),
      body: JSON.stringify({ newPassword, confirmPassword }),
    });

    if (!response.ok) {
      throw new Error("Failed to save password");
    }

    closePasswordSettingsModal();
    notifySettings("Password updated");
  } catch (error) {
    console.error("Error saving password:", error);
    notifySettings("Unable to update password");
  }
}

function openPasswordSettingsModal() {
  const modal = document.getElementById("passwordSettingsModal");
  const form = document.getElementById("passwordSettingsForm");

  if (!modal) {
    return;
  }

  form?.reset();
  modal.classList.add("active");
  modal.setAttribute("aria-hidden", "false");
  document.body.classList.add("modal-open");
  document.getElementById("settingsNewPassword")?.focus();
}

function closePasswordSettingsModal() {
  const modal = document.getElementById("passwordSettingsModal");

  if (!modal) {
    return;
  }

  modal.classList.remove("active");
  modal.setAttribute("aria-hidden", "true");
  document.body.classList.remove("modal-open");
}

function getSettingsHeaders() {
  const token = localStorage.getItem("jwtToken");
  const headers = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return headers;
}

function syncHeaderUser(user) {
  const clientName = document.getElementById("clientName");
  const clientAvatar = document.getElementById("clientAvatar");
  const displayName = getUserDisplayName(user);

  if (clientName) {
    clientName.textContent = displayName;
  }

  if (clientAvatar) {
    clientAvatar.textContent =
      String(displayName || "Client").trim().charAt(0).toUpperCase() || "C";
    clientAvatar.setAttribute("aria-label", `${displayName} profile`);
  }
}

function updateStoredToken(token) {
  if (!token) {
    return;
  }

  localStorage.setItem("jwtToken", token);
  document.cookie = `jwtToken=${token}; path=/; samesite=strict`;
}

function getUserDisplayName(user) {
  return (
    [user.firstName, user.lastName].filter(Boolean).join(" ") ||
    user.email ||
    "User"
  );
}

function notifySettings(message) {
  const existingToast = document.querySelector(".toast-message");
  if (existingToast) {
    existingToast.remove();
  }

  document.body.insertAdjacentHTML(
    "afterbegin",
    `
      <div class="toast-message" role="status">
        <p>${escapeHtml(message)}</p>
        <button type="button" onclick="this.closest('.toast-message').remove()">OK</button>
      </div>
    `,
  );

  window.setTimeout(() => {
    document.querySelector(".toast-message")?.remove();
  }, 4200);
}
