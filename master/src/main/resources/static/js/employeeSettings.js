(function () {
  let initialState = null;
  let loaded = false;

  document.addEventListener("DOMContentLoaded", function () {
    setupSettingsForm();
    setupPasswordModal();

    document
      .querySelector('[data-cat="settings"]')
      ?.addEventListener("click", function () {
        loadEmployeeSettings();
      });
  });

  function setupSettingsForm() {
    const form = document.getElementById("employeeSettingsForm");

    if (!form) return;

    form.addEventListener("input", updateSaveState);
    form.addEventListener("submit", saveEmployeeSettings);
  }

  function setupPasswordModal() {
    document
      .getElementById("employeeOpenPasswordModal")
      ?.addEventListener("click", openPasswordModal);

    document.addEventListener("click", function (event) {
      if (event.target.closest("[data-employee-settings-modal-close]")) {
        closePasswordModal();
      }
    });

    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape") {
        closePasswordModal();
      }
    });

    document
      .getElementById("employeePasswordSettingsForm")
      ?.addEventListener("submit", saveEmployeePassword);
  }

  async function loadEmployeeSettings(forceRefresh = false) {
    if (loaded && !forceRefresh) return;

    try {
      const response = await fetch("/api/v1/users/me", {
        headers: getHeaders(),
      });

      if (!response.ok) {
        throw new Error("Unable to load settings");
      }

      const user = await response.json();
      loaded = true;
      fillSettings(user);
      syncEmployeeHeader(user);
    } catch (error) {
      console.error("Error loading employee settings:", error);
      notify("Unable to load settings", "error");
    }
  }

  function fillSettings(user) {
    const firstName = document.getElementById("employeeSettingsFirstName");
    const lastName = document.getElementById("employeeSettingsLastName");
    const email = document.getElementById("employeeSettingsEmail");
    const displayName = document.getElementById("employeeSettingsDisplayName");

    if (!firstName || !lastName || !email) return;

    firstName.value = user.firstName || "";
    lastName.value = user.lastName || "";
    email.value = user.email || "";

    initialState = readState();

    if (displayName) {
      displayName.textContent = getDisplayName(initialState);
    }

    updateSaveState();
  }

  function readState() {
    return {
      firstName:
        document.getElementById("employeeSettingsFirstName")?.value.trim() || "",
      lastName:
        document.getElementById("employeeSettingsLastName")?.value.trim() || "",
      email: document.getElementById("employeeSettingsEmail")?.value.trim() || "",
    };
  }

  function updateSaveState() {
    const saveButton = document.getElementById("employeeSettingsSaveButton");

    if (!saveButton || !initialState) return;

    const currentState = readState();
    saveButton.disabled =
      currentState.firstName === initialState.firstName &&
      currentState.lastName === initialState.lastName &&
      currentState.email === initialState.email;
  }

  async function saveEmployeeSettings(event) {
    event.preventDefault();

    const saveButton = document.getElementById("employeeSettingsSaveButton");
    const payload = readState();

    if (saveButton?.disabled) return;

    try {
      if (saveButton) saveButton.disabled = true;

      const response = await fetch("/api/v1/users/me", {
        method: "PUT",
        headers: getHeaders(),
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error("Unable to save settings");
      }

      const user = await response.json();
      updateStoredToken(user.token);
      fillSettings(user);
      syncEmployeeHeader(user);
      notify("Settings saved", "success");
    } catch (error) {
      console.error("Error saving employee settings:", error);
      notify("Unable to save settings", "error");
      updateSaveState();
    }
  }

  async function saveEmployeePassword(event) {
    event.preventDefault();

    const newPassword =
      document.getElementById("employeeSettingsNewPassword")?.value || "";
    const confirmPassword =
      document.getElementById("employeeSettingsConfirmPassword")?.value || "";

    if (newPassword !== confirmPassword) {
      notify("Passwords do not match", "error");
      return;
    }

    try {
      const response = await fetch("/api/v1/users/me/password", {
        method: "PUT",
        headers: getHeaders(),
        body: JSON.stringify({ newPassword, confirmPassword }),
      });

      if (!response.ok) {
        throw new Error("Unable to update password");
      }

      closePasswordModal();
      notify("Password updated", "success");
    } catch (error) {
      console.error("Error updating employee password:", error);
      notify("Unable to update password", "error");
    }
  }

  function openPasswordModal() {
    const modal = document.getElementById("employeePasswordSettingsModal");
    const form = document.getElementById("employeePasswordSettingsForm");

    if (!modal) return;

    form?.reset();
    modal.classList.add("active");
    modal.setAttribute("aria-hidden", "false");
    document.body.classList.add("modal-open");
    document.getElementById("employeeSettingsNewPassword")?.focus();
  }

  function closePasswordModal() {
    const modal = document.getElementById("employeePasswordSettingsModal");

    if (!modal) return;

    modal.classList.remove("active");
    modal.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");
  }

  function getHeaders() {
    const token = localStorage.getItem("jwtToken");
    const headers = {
      "Content-Type": "application/json",
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    return headers;
  }

  function syncEmployeeHeader(user) {
    const displayName = getDisplayName(user);
    const headerName = document.getElementById("employeeHeaderName");

    if (headerName) {
      headerName.textContent = displayName;
    }
  }

  function updateStoredToken(token) {
    if (!token) return;

    localStorage.setItem("jwtToken", token);
    document.cookie = `jwtToken=${token}; path=/; samesite=strict`;
  }

  function getDisplayName(user) {
    return (
      [user.firstName, user.lastName].filter(Boolean).join(" ") ||
      user.email ||
      "User"
    );
  }

  function notify(message, type) {
    if (typeof window.showNotification === "function") {
      window.showNotification(message, type);
      return;
    }

    window.alert(message);
  }
})();
