// Handle Add Shift form submission
runWhenReady(function () {
  setupStaffNavigation();
  const addShiftForm = document.getElementById("addShiftForm");
  setupEmployeeLogout();

  if (addShiftForm) {
    addShiftForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      const formData = new FormData(addShiftForm);
      const selectedDate = formData.get("date");
      const employeeId = formData.get("employeeId");
      const position = formData.get("position");
      const startTime = formData.get("startTime");
      const endTime = formData.get("endTime");

      // Validate required fields
      if (!employeeId) {
        alert("Please select an employee");
        return;
      }
      if (!selectedDate) {
        alert("Please select a date");
        return;
      }

      // Validate time comparison
      if (startTime && endTime && startTime >= endTime) {
        alert("Start time cannot be later than or equal to end time");
        return;
      }
      if (!position) {
        alert("Please select a position");
        return;
      }
      if (!startTime) {
        alert("Please enter a start time");
        return;
      }
      if (!endTime) {
        alert("Please enter an end time");
        return;
      }

      // Calculate weekday (1-7, where 1=Monday, 7=Sunday)
      // Parse date as local date to avoid timezone issues
      const dateParts = selectedDate.split("-");
      const date = new Date(
        parseInt(dateParts[0]),
        parseInt(dateParts[1]) - 1,
        parseInt(dateParts[2]),
      );
      let weekday = date.getDay(); // 0=Sunday, 1=Monday, ..., 6=Saturday
      weekday = weekday === 0 ? 7 : weekday; // Convert to 1-7 format (1=Monday, 7=Sunday)

      const shiftData = {
        employeeId: parseInt(employeeId),
        date: selectedDate,
        weekday: weekday,
        position: position,
        startTime: startTime,
        endTime: endTime,
      };

      console.log("Adding new shift:", shiftData);

      try {
        const response = await fetch("/api/planning/shifts", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + localStorage.getItem("jwtToken"),
          },
          body: JSON.stringify(shiftData),
        });

        const result = await response.json();

        if (result.success) {
          console.log("Shift added successfully with ID:", result.id);

          // Close modal and refresh the planning view
          const modal = bootstrap.Modal.getInstance(
            document.getElementById("addShiftModal"),
          );
          modal.hide();
          addShiftForm.reset();

          // Refresh the planning data if Alpine.js is available
          if (window.Alpine) {
            const planningComponent =
              document.getElementById("planningInterface");
            if (planningComponent && planningComponent._x_dataStack) {
              planningComponent._x_dataStack[0].loadSchedule();
            }
          }

          alert("Shift added successfully!");
        } else {
          alert("Error adding shift: " + result.message);
        }
      } catch (error) {
        console.error("Error adding shift:", error);
        alert("Error adding shift. Please try again.");
      }
    });
  }
});

function runWhenReady(callback) {
  if (document.readyState === "loading" && !document.body) {
    document.addEventListener("DOMContentLoaded", callback, { once: true });
    return;
  }

  callback();
}

function setupStaffNavigation() {
  showStaffSection(
    document.querySelector(".nav-btn.active[data-cat]")?.dataset.cat ||
      "booking",
  );

  document.addEventListener("click", (event) => {
    const button = event.target.closest(".nav-btn[data-cat]");

    if (!button || button.matches("[data-logout-link]")) {
      return;
    }

    event.preventDefault();
    showStaffSection(button.dataset.cat);
  });
}

function showStaffSection(category) {
  document.querySelectorAll(".nav-btn[data-cat]").forEach((button) => {
    button.classList.toggle("active", button.dataset.cat === category);
  });

  document.querySelectorAll(".main-card[data-cat]").forEach((section) => {
    section.classList.toggle("hidden", section.dataset.cat !== category);
  });

  window.scrollTo({ top: 0, left: 0, behavior: "auto" });
}

function setupEmployeeLogout() {
  document.addEventListener("click", async function (event) {
    const logoutLink = event.target.closest("[data-logout-link]");

    if (!logoutLink) {
      return;
    }

    event.preventDefault();
    const token = getStaffJwtToken();

    if (token) {
      try {
        await fetch("/api/v1/logout", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });
      } catch (error) {
        console.error("Logout request failed:", error);
      }
    }

    localStorage.removeItem("jwtToken");
    localStorage.removeItem("userRole");
    document.cookie = "jwtToken=; path=/; max-age=0; samesite=strict";
    window.location.href = "/";
  });
}

// Global utility functions for leave requests and other features
window.showNotification = function (message, type) {
  // Create a simple notification system
  const notification = document.createElement("div");
  notification.className = `alert alert-${type === "error" ? "danger" : type === "success" ? "success" : "info"} alert-dismissible fade show`;
  notification.style.cssText = "min-width: 300px; margin-bottom: 10px;";
  notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

  // Add to notification container or body
  const container =
    document.getElementById("notificationContainer") || document.body;
  container.appendChild(notification);

  // Auto-remove after 5 seconds
  setTimeout(() => {
    if (notification.parentNode) {
      notification.parentNode.removeChild(notification);
    }
  }, 5000);
};

window.isCurrentUserAdmin = function () {
  // Check if current user has admin privileges
  // This can be determined by checking if the user can see admin-only sections
  const approvalSection = document.querySelector('[data-cat="approval"]');
  const employeeSection = document.querySelector('[data-cat="employees"]');
  return approvalSection && employeeSection; // If both admin sections exist, user is admin
};

window.showMessage = window.showNotification; // Alias for compatibility

function getStaffJwtToken() {
  const cookieToken = document.cookie
    .split("; ")
    .find((row) => row.startsWith("jwtToken="))
    ?.split("=")[1];

  return cookieToken || localStorage.getItem("jwtToken") || "";
}
