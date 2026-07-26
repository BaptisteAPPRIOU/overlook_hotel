/**
 * Leave Request Management JavaScript
 * Handles employee leave request operations in the employee dashboard
 */

// Global variables
let currentEmployeeId = null;
let leaveRequests = [];

/**
 * Fetch with authentication helper function
 */
async function fetchWithAuth(url, options = {}) {
  const token = localStorage.getItem("jwtToken");

  if (!token) {
    alert("No JWT token found, please login.");
    window.location.href = "/employeeLogin";
    throw new Error("No JWT token found");
  }

  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
    Authorization: "Bearer " + token,
  };

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401) {
    alert("Session expired. Please login again.");
    window.location.href = "/employeeLogin";
    throw new Error("Unauthorized");
  }

  if (response.status === 403) {
    throw new Error("Forbidden");
  }

  return response;
}

/**
 * Initialize leave request functionality when page loads
 */
document.addEventListener("DOMContentLoaded", function () {
  initializeLeaveRequestSection();

  if (localStorage.getItem("userRole") !== "ADMIN") {
    loadMyLeaveRequests();
  }

  // If user is admin, also load pending requests for approval
  if (isCurrentUserAdmin()) {
    loadPendingLeaveRequests();
    loadAllLeaveRequests();
  }
});

/**
 * Initialize the leave request form and event listeners
 */
function initializeLeaveRequestSection() {
  const leaveRequestForm = document.getElementById("leaveRequestForm");

  if (leaveRequestForm) {
    leaveRequestForm.addEventListener("submit", function (e) {
      e.preventDefault();
      handleSubmitLeaveRequest();
    });
  }

  document.querySelectorAll("[data-date-picker]").forEach((button) => {
    button.addEventListener("click", function () {
      const dateInput = document.getElementById(this.dataset.datePicker);

      if (!dateInput) return;

      if (typeof dateInput.showPicker === "function") {
        try {
          dateInput.showPicker();
        } catch (error) {
          dateInput.focus();
        }
      } else {
        dateInput.focus();
      }
    });
  });

  // Initialize date validation
  const startDateInput = document.getElementById("leaveStartDate");
  const endDateInput = document.getElementById("leaveEndDate");

  if (startDateInput) {
    startDateInput.addEventListener("change", validateLeaveDates);
    // Set minimum date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    startDateInput.min = tomorrow.toISOString().split("T")[0];
  }

  if (endDateInput) {
    endDateInput.addEventListener("change", validateLeaveDates);
  }

  updateLeaveDateDisplays();

  // Initialize character counter for reason textarea
  const reasonTextarea = document.getElementById("leaveReason");
  const charCountSpan = document.getElementById("reasonCharCount");

  if (reasonTextarea && charCountSpan) {
    reasonTextarea.addEventListener("input", function () {
      const currentLength = this.value.length;
      charCountSpan.textContent = currentLength;

      // Change color based on character count
      if (currentLength > 450) {
        charCountSpan.style.color = "#dc3545"; // Red
      } else if (currentLength > 400) {
        charCountSpan.style.color = "#fd7e14"; // Orange
      } else {
        charCountSpan.style.color = "#6c757d"; // Default gray
      }
    });
  }
}

/**
 * Handle leave request form submission
 */
async function handleSubmitLeaveRequest() {
  try {
    // Get form data
    const startDate = document.getElementById("leaveStartDate").value;
    const endDate = document.getElementById("leaveEndDate").value;
    const reasonInput = document.getElementById("leaveReason");
    const startPeriod = document.getElementById("leaveStartPeriod")?.value;
    const endPeriod = document.getElementById("leaveEndPeriod")?.value;
    const reason = buildLeaveReason(reasonInput?.value, startPeriod, endPeriod);
    const type = document.getElementById("leaveType").value;

    // Validate form data
    if (!validateLeaveRequestForm(startDate, endDate, reason, type)) {
      return;
    }

    // Prepare request data
    const requestData = {
      startDate: startDate,
      endDate: endDate,
      reason: reason.trim(),
      type: type.toUpperCase(),
    };

    // Show loading state
    const submitButton = document.getElementById("submitLeaveRequestBtn");
    const originalText = submitButton.dataset.defaultText || submitButton.textContent;
    submitButton.disabled = true;
    submitButton.textContent = "Saving...";

    // Submit leave request
    const response = await fetchWithAuth("/api/v1/leave-requests/submit", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(requestData),
    });

    const result = await response.json();

    if (response.ok) {
      // Success
      showNotification("Leave request submitted successfully!", "success");
      resetLeaveRequestForm();
      loadMyLeaveRequests(); // Refresh the list
    } else {
      // Error
      showNotification(
        result.message || "Failed to submit leave request",
        "error",
      );
    }
  } catch (error) {
    console.error("Error submitting leave request:", error);
    showNotification("Network error. Please try again.", "error");
  } finally {
    // Reset button state
    const submitButton = document.getElementById("submitLeaveRequestBtn");
    submitButton.disabled = false;
    submitButton.textContent = submitButton.dataset.defaultText || "Save";
  }
}

function buildLeaveReason(reason, startPeriod, endPeriod) {
  const baseReason = (reason || "").trim();
  const periodSummary =
    startPeriod || endPeriod
      ? `Starting from ${startPeriod || "-"}; up to ${endPeriod || "-"}.`
      : "";

  return [periodSummary, baseReason].filter(Boolean).join(" ");
}

/**
 * Validate leave request form data
 */
function validateLeaveRequestForm(startDate, endDate, reason, type) {
  // Check required fields
  if (!startDate) {
    showNotification("Please select a start date", "error");
    return false;
  }

  if (!endDate) {
    showNotification("Please select an end date", "error");
    return false;
  }

  if (!type) {
    showNotification("Please select a leave type", "error");
    return false;
  }

  // Validate dates
  const start = new Date(startDate);
  const end = new Date(endDate);
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (start <= today) {
    showNotification("Start date must be in the future", "error");
    return false;
  }

  if (end < start) {
    showNotification("End date must be after start date", "error");
    return false;
  }

  // Check leave duration (max 30 days)
  const daysDifference = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
  if (daysDifference > 30) {
    showNotification("Leave duration cannot exceed 30 days", "error");
    return false;
  }

  // Validate reason length
  if (reason && reason.trim().length > 500) {
    showNotification("Reason cannot exceed 500 characters", "error");
    return false;
  }

  return true;
}

/**
 * Validate date inputs when they change
 */
function validateLeaveDates() {
  const startDateInput = document.getElementById("leaveStartDate");
  const endDateInput = document.getElementById("leaveEndDate");

  const startDate = startDateInput.value;
  const endDate = endDateInput.value;

  if (startDate) {
    // Set end date minimum to start date
    endDateInput.min = startDate;

    // If end date is before start date, clear it
    if (endDate && new Date(endDate) < new Date(startDate)) {
      endDateInput.value = "";
    }
  }

  updateLeaveDateDisplays();
}

function updateLeaveDateDisplays() {
  updateLeaveDateDisplay("leaveStartDate", "leaveStartDateDisplay");
  updateLeaveDateDisplay("leaveEndDate", "leaveEndDateDisplay");
}

function updateLeaveDateDisplay(inputId, displayId) {
  const input = document.getElementById(inputId);
  const display = document.getElementById(displayId);

  if (!input || !display) return;

  display.value = input.value ? formatDateForInput(input.value) : "";
}

function formatDateForInput(value) {
  const [year, month, day] = value.split("-");

  if (!year || !month || !day) return value;

  return `${day} / ${month} / ${year}`;
}

/**
 * Load current employee's leave requests
 */
async function loadMyLeaveRequests() {
  try {
    const response = await fetchWithAuth("/api/v1/leave-requests/my-requests");

    if (response.ok) {
      const result = await response.json();
      // Extract the data array from the API response
      const requests = result.data || [];
      leaveRequests = requests;
      displayMyLeaveRequests(requests);
    } else {
      console.error("Failed to load leave requests");
      displayMyLeaveRequests([]);
    }
  } catch (error) {
    if (error.message !== "Forbidden") {
      console.error("Error loading leave requests:", error);
    }
    displayMyLeaveRequests([]);
  }
}

/**
 * Load pending leave requests for admin approval
 */
async function loadPendingLeaveRequests() {
  try {
    const response = await fetchWithAuth("/api/v1/leave-requests/pending");

    if (response.ok) {
      const result = await response.json();
      // Extract the data array from the API response
      const requests = result.data || [];
      displayPendingLeaveRequests(requests);
    } else {
      console.error("Failed to load pending requests");
      displayPendingLeaveRequests([]);
    }
  } catch (error) {
    if (error.message !== "Forbidden") {
      console.error("Error loading pending requests:", error);
    }
    displayPendingLeaveRequests([]);
  }
}

/**
 * Load all leave requests for admin oversight
 */
async function loadAllLeaveRequests() {
  try {
    const response = await fetchWithAuth("/api/v1/leave-requests/all");

    if (response.ok) {
      const result = await response.json();
      // Extract the data array from the API response
      const requests = result.data || [];
      displayAllLeaveRequests(requests);
    } else {
      console.error("Failed to load all requests");
      displayAllLeaveRequests([]);
    }
  } catch (error) {
    if (error.message !== "Forbidden") {
      console.error("Error loading all requests:", error);
    }
    displayAllLeaveRequests([]);
  }
}

/**
 * Display employee's leave requests in the table
 */
function displayMyLeaveRequests(requests) {
  const tableBody = document.querySelector("#myLeaveRequestsTable tbody");
  if (!tableBody) return;

  tableBody.innerHTML = "";

  if (requests.length === 0) {
    const row = tableBody.insertRow();
    const cell = row.insertCell(0);
    cell.colSpan = 6;
    cell.className = "text-center";
    cell.textContent = "No leave requests found";
    return;
  }

  requests.forEach((request) => {
    const row = tableBody.insertRow();

    // Type
    const typeCell = row.insertCell(0);
    typeCell.textContent = formatLeaveType(request.type);

    // Dates
    const datesCell = row.insertCell(1);
    datesCell.textContent = `${formatDate(request.startDate)} - ${formatDate(request.endDate)}`;

    // Duration
    const durationCell = row.insertCell(2);
    durationCell.textContent = `${calculateLeaveDuration(request.startDate, request.endDate)} days`;

    // Status
    const statusCell = row.insertCell(3);
    statusCell.innerHTML = `<span class="status-badge status-${request.status.toLowerCase()}">${request.status}</span>`;

    // Submitted
    const submittedCell = row.insertCell(4);
    submittedCell.textContent = formatDateTime(request.createdAt);

    // Actions
    const actionsCell = row.insertCell(5);
    if (request.status === "PENDING") {
      actionsCell.innerHTML = `
                <button class="btn btn-sm btn-outline-danger" onclick="cancelLeaveRequest(${request.id})">
                    Cancel
                </button>
            `;
    } else {
      actionsCell.textContent = "-";
    }
  });
}

/**
 * Display pending leave requests for admin approval
 */
function displayPendingLeaveRequests(requests) {
  const list = document.getElementById("pendingLeaveRequestsList");
  const pendingCount = document.getElementById("pendingLeaveCount");

  if (pendingCount) {
    pendingCount.textContent = requests.length;
  }

  if (list) {
    if (requests.length === 0) {
      list.innerHTML =
        '<div class="employee-leave-approval-empty">No pending requests.</div>';
      return;
    }

    list.innerHTML = requests
      .map((request) => renderLeaveApprovalItem(request, "pending"))
      .join("");
    return;
  }

  const tableBody = document.querySelector("#pendingLeaveRequestsTable tbody");
  if (!tableBody) return;

  tableBody.innerHTML = "";

  if (requests.length === 0) {
    const row = tableBody.insertRow();
    const cell = row.insertCell(0);
    cell.colSpan = 7;
    cell.className = "text-center";
    cell.textContent = "No pending requests";
    return;
  }

  requests.forEach((request) => {
    const row = tableBody.insertRow();

    // Employee
    const employeeCell = row.insertCell(0);
    employeeCell.textContent =
      request.employeeName || `Employee ${request.employeeId}`;

    // Type
    const typeCell = row.insertCell(1);
    typeCell.textContent = formatLeaveType(request.type);

    // Dates
    const datesCell = row.insertCell(2);
    datesCell.textContent = `${formatDate(request.startDate)} - ${formatDate(request.endDate)}`;

    // Duration
    const durationCell = row.insertCell(3);
    durationCell.textContent = `${calculateLeaveDuration(request.startDate, request.endDate)} days`;

    // Reason
    const reasonCell = row.insertCell(4);
    reasonCell.textContent = request.reason || "-";
    reasonCell.title = request.reason || "";

    // Submitted
    const submittedCell = row.insertCell(5);
    submittedCell.textContent = formatDateTime(request.createdAt);

    // Actions
    const actionsCell = row.insertCell(6);
    actionsCell.innerHTML = `
            <button class="btn btn-sm btn-success me-1" onclick="approveLeaveRequest(${request.id})">
                Approve
            </button>
            <button class="btn btn-sm btn-danger" onclick="rejectLeaveRequest(${request.id})">
                Reject
            </button>
        `;
  });
}

/**
 * Display all leave requests for admin oversight
 */
function displayAllLeaveRequests(requests) {
  const list = document.getElementById("allLeaveRequestsList");
  const allCount = document.getElementById("allLeaveCount");

  if (allCount) {
    allCount.textContent = requests.length;
  }

  if (list) {
    if (requests.length === 0) {
      list.innerHTML =
        '<div class="employee-leave-approval-empty">No leave requests found.</div>';
      return;
    }

    list.innerHTML = requests
      .map((request) => renderLeaveApprovalItem(request, "all"))
      .join("");
    return;
  }

  const tableBody = document.querySelector("#allLeaveRequestsTable tbody");
  if (!tableBody) return;

  tableBody.innerHTML = "";

  if (requests.length === 0) {
    const row = tableBody.insertRow();
    const cell = row.insertCell(0);
    cell.colSpan = 8;
    cell.className = "text-center";
    cell.textContent = "No leave requests found";
    return;
  }

  requests.forEach((request) => {
    const row = tableBody.insertRow();

    // Employee
    const employeeCell = row.insertCell(0);
    employeeCell.textContent =
      request.employeeName || `Employee ${request.employeeId}`;

    // Type
    const typeCell = row.insertCell(1);
    typeCell.textContent = formatLeaveType(request.type);

    // Dates
    const datesCell = row.insertCell(2);
    datesCell.textContent = `${formatDate(request.startDate)} - ${formatDate(request.endDate)}`;

    // Duration
    const durationCell = row.insertCell(3);
    durationCell.textContent = `${calculateLeaveDuration(request.startDate, request.endDate)} days`;

    // Status
    const statusCell = row.insertCell(4);
    statusCell.innerHTML = `<span class="status-badge status-${request.status.toLowerCase()}">${request.status}</span>`;

    // Submitted
    const submittedCell = row.insertCell(5);
    submittedCell.textContent = formatDateTime(request.createdAt);

    // Processed By
    const processedByCell = row.insertCell(6);
    if (request.status === "APPROVED") {
      processedByCell.textContent = request.approvedBy || "-";
    } else if (request.status === "REJECTED") {
      processedByCell.textContent = request.rejectedBy || "-";
    } else {
      processedByCell.textContent = "-";
    }

    // Actions
    const actionsCell = row.insertCell(7);
    if (request.status === "PENDING") {
      actionsCell.innerHTML = `
                <button class="btn btn-sm btn-success me-1" onclick="approveLeaveRequest(${request.id})">
                    Approve
                </button>
                <button class="btn btn-sm btn-danger" onclick="rejectLeaveRequest(${request.id})">
                    Reject
                </button>
            `;
    } else {
      actionsCell.innerHTML = `<span class="text-muted">Processed</span>`;
    }
  });
}

function renderLeaveApprovalItem(request, mode) {
  const employeeName =
    request.employeeName || `Employee ${request.employeeId || "-"}`;
  const dates = `${formatDate(request.startDate)} - ${formatDate(request.endDate)}`;
  const duration = `${calculateLeaveDuration(request.startDate, request.endDate)} days`;
  const status = request.status || "PENDING";
  const processedBy =
    status === "APPROVED"
      ? request.approvedBy || "-"
      : status === "REJECTED"
        ? request.rejectedBy || "-"
        : "-";

  if (mode === "pending") {
    return `
      <article class="employee-leave-approval-item">
        ${leaveApprovalField("Employee", employeeName)}
        ${leaveApprovalField("Type", formatLeaveType(request.type))}
        ${leaveApprovalField("Dates", dates)}
        ${leaveApprovalField("Reason", request.reason || "-")}
        ${leaveApprovalActions(request)}
      </article>
    `;
  }

  return `
    <article class="employee-leave-approval-item compact">
      ${leaveApprovalField("Employee", employeeName)}
      ${leaveApprovalField("Dates", dates)}
      ${leaveApprovalField("Duration", duration)}
      <div class="employee-leave-approval-field">
        <span class="employee-leave-approval-label">Status</span>
        <span class="employee-leave-status ${status.toLowerCase()}">${escapeHtml(status)}</span>
      </div>
      ${
        status === "PENDING"
          ? leaveApprovalActions(request)
          : leaveApprovalField("Processed by", processedBy)
      }
    </article>
  `;
}

function leaveApprovalField(label, value) {
  return `
    <div class="employee-leave-approval-field">
      <span class="employee-leave-approval-label">${escapeHtml(label)}</span>
      <span class="employee-leave-approval-value">${escapeHtml(value)}</span>
    </div>
  `;
}

function leaveApprovalActions(request) {
  return `
    <div class="employee-leave-approval-actions">
      <button
        class="employee-leave-action approve"
        type="button"
        onclick="approveLeaveRequest(${Number(request.id)})">
        Approve
      </button>
      <button
        class="employee-leave-action reject"
        type="button"
        onclick="rejectLeaveRequest(${Number(request.id)})">
        Reject
      </button>
    </div>
  `;
}

/**
 * Cancel a leave request
 */
async function cancelLeaveRequest(requestId) {
  if (!confirm("Are you sure you want to cancel this leave request?")) {
    return;
  }

  try {
    const response = await fetchWithAuth(
      `/api/v1/leave-requests/${requestId}`,
      {
        method: "DELETE",
      },
    );

    const result = await response.json();

    if (response.ok) {
      showNotification("Leave request cancelled successfully", "success");
      loadMyLeaveRequests(); // Refresh the list
    } else {
      showNotification(
        result.message || "Failed to cancel leave request",
        "error",
      );
    }
  } catch (error) {
    console.error("Error cancelling leave request:", error);
    showNotification("Network error. Please try again.", "error");
  }
}

/**
 * Approve a leave request (admin only)
 */
async function approveLeaveRequest(requestId) {
  if (!confirm("Are you sure you want to approve this leave request?")) {
    return;
  }

  try {
    const response = await fetchWithAuth(
      `/api/v1/leave-requests/${requestId}/approve`,
      {
        method: "PUT",
      },
    );

    const result = await response.json();

    if (response.ok) {
      showNotification("Leave request approved successfully", "success");
      loadPendingLeaveRequests(); // Refresh pending list
      loadAllLeaveRequests(); // Refresh all requests list
    } else {
      showNotification(
        result.message || "Failed to approve leave request",
        "error",
      );
    }
  } catch (error) {
    console.error("Error approving leave request:", error);
    showNotification("Network error. Please try again.", "error");
  }
}

/**
 * Reject a leave request (admin only)
 */
async function rejectLeaveRequest(requestId) {
  const reason = prompt("Please enter the reason for rejection:");
  if (!reason || reason.trim() === "") {
    return;
  }

  try {
    const response = await fetchWithAuth(
      `/api/v1/leave-requests/${requestId}/reject`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ reason: reason.trim() }),
      },
    );

    const result = await response.json();

    if (response.ok) {
      showNotification("Leave request rejected", "success");
      loadPendingLeaveRequests(); // Refresh pending list
      loadAllLeaveRequests(); // Refresh all requests list
    } else {
      showNotification(
        result.message || "Failed to reject leave request",
        "error",
      );
    }
  } catch (error) {
    console.error("Error rejecting leave request:", error);
    showNotification("Network error. Please try again.", "error");
  }
}

/**
 * Reset the leave request form
 */
function resetLeaveRequestForm() {
  document.getElementById("leaveStartDate").value = "";
  document.getElementById("leaveEndDate").value = "";
  document.getElementById("leaveReason").value = "";
  document.getElementById("leaveType").selectedIndex = 0;
  const startPeriod = document.getElementById("leaveStartPeriod");
  const endPeriod = document.getElementById("leaveEndPeriod");

  if (startPeriod) {
    startPeriod.value = "Noon";
  }

  if (endPeriod) {
    endPeriod.value = "Morning";
  }

  updateLeaveDateDisplays();
}

/**
 * Utility functions
 */

function formatLeaveType(type) {
  const types = {
    VACATION: "Vacation",
    SICK: "Sick Leave",
    PERSONAL: "Personal Leave",
    MATERNITY: "Maternity Leave",
    PATERNITY: "Paternity Leave",
    BEREAVEMENT: "Bereavement Leave",
    EMERGENCY: "Emergency Leave",
    STUDY: "Study Leave",
    UNPAID: "Unpaid Leave",
  };
  return types[type] || type;
}

function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function formatDateTime(dateTimeString) {
  const date = new Date(dateTimeString);
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function calculateLeaveDuration(startDate, endDate) {
  const start = new Date(startDate);
  const end = new Date(endDate);
  const diffTime = Math.abs(end - start);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
  return diffDays;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function isCurrentUserAdmin() {
  const role = localStorage.getItem("userRole");
  return role === "RESPONSABLE" || role === "ADMIN";
}

function showNotification(message, type = "info") {
  // Create notification container if it doesn't exist
  let container = document.getElementById("notificationContainer");
  if (!container) {
    container = document.createElement("div");
    container.id = "notificationContainer";
    container.className = "position-fixed";
    container.style.cssText = "top: 20px; right: 20px; z-index: 9999;";
    document.body.appendChild(container);
  }

  // Create notification element
  const notification = document.createElement("div");
  notification.className = `alert alert-${type === "error" ? "danger" : type === "success" ? "success" : "info"} alert-dismissible fade show`;
  notification.style.cssText = "min-width: 300px; margin-bottom: 10px;";

  notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

  container.appendChild(notification);

  // Auto-remove after 5 seconds
  setTimeout(() => {
    if (notification.parentNode) {
      notification.remove();
    }
  }, 5000);
}

// Make functions globally available
window.cancelLeaveRequest = cancelLeaveRequest;
window.approveLeaveRequest = approveLeaveRequest;
window.rejectLeaveRequest = rejectLeaveRequest;
