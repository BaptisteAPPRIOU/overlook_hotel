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
    const token = localStorage.getItem('jwtToken');
    
    if (!token) {
        alert('No JWT token found, please login.');
        window.location.href = '/employeeLogin';
        throw new Error('No JWT token found');
    }
    
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
        'Authorization': 'Bearer ' + token
    };
    
    const response = await fetch(url, { ...options, headers });
    
    if (response.status === 403) {
        alert('Access denied. Please login again.');
        window.location.href = '/employeeLogin';
        throw new Error('Forbidden');
    }
    
    return response;
}

/**
 * Initialize leave request functionality when page loads
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeLeaveRequestSection();
    loadMyLeaveRequests();
    
    // If user is admin, also load pending requests for approval
    if (isCurrentUserAdmin()) {
        loadPendingLeaveRequests();
    }
});

/**
 * Initialize the leave request form and event listeners
 */
function initializeLeaveRequestSection() {
    const submitButton = document.getElementById('submitLeaveRequestBtn');
    const leaveRequestForm = document.getElementById('leaveRequestForm');
    
    if (submitButton) {
        submitButton.addEventListener('click', handleSubmitLeaveRequest);
    }
    
    if (leaveRequestForm) {
        leaveRequestForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleSubmitLeaveRequest();
        });
    }
    
    // Initialize date validation
    const startDateInput = document.getElementById('leaveStartDate');
    const endDateInput = document.getElementById('leaveEndDate');
    
    if (startDateInput) {
        startDateInput.addEventListener('change', validateLeaveDates);
        // Set minimum date to tomorrow
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        startDateInput.min = tomorrow.toISOString().split('T')[0];
    }
    
    if (endDateInput) {
        endDateInput.addEventListener('change', validateLeaveDates);
    }
    
    // Initialize character counter for reason textarea
    const reasonTextarea = document.getElementById('leaveReason');
    const charCountSpan = document.getElementById('reasonCharCount');
    
    if (reasonTextarea && charCountSpan) {
        reasonTextarea.addEventListener('input', function() {
            const currentLength = this.value.length;
            charCountSpan.textContent = currentLength;
            
            // Change color based on character count
            if (currentLength > 450) {
                charCountSpan.style.color = '#dc3545'; // Red
            } else if (currentLength > 400) {
                charCountSpan.style.color = '#fd7e14'; // Orange
            } else {
                charCountSpan.style.color = '#6c757d'; // Default gray
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
        const startDate = document.getElementById('leaveStartDate').value;
        const endDate = document.getElementById('leaveEndDate').value;
        const reason = document.getElementById('leaveReason').value;
        const type = document.getElementById('leaveType').value;
        
        // Validate form data
        if (!validateLeaveRequestForm(startDate, endDate, reason, type)) {
            return;
        }
        
        // Prepare request data
        const requestData = {
            startDate: startDate,
            endDate: endDate,
            reason: reason.trim(),
            type: type.toUpperCase()
        };
        
        // Show loading state
        const submitButton = document.getElementById('submitLeaveRequestBtn');
        const originalText = submitButton.textContent;
        submitButton.disabled = true;
        submitButton.textContent = 'Submitting...';
        
        // Submit leave request
        const response = await fetchWithAuth('/api/v1/leave-requests/submit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });
        
        const result = await response.json();
        
        if (response.ok) {
            // Success
            showNotification('Leave request submitted successfully!', 'success');
            resetLeaveRequestForm();
            loadMyLeaveRequests(); // Refresh the list
        } else {
            // Error
            showNotification(result.message || 'Failed to submit leave request', 'error');
        }
        
    } catch (error) {
        console.error('Error submitting leave request:', error);
        showNotification('Network error. Please try again.', 'error');
    } finally {
        // Reset button state
        const submitButton = document.getElementById('submitLeaveRequestBtn');
        submitButton.disabled = false;
        submitButton.textContent = 'Submit Request';
    }
}

/**
 * Validate leave request form data
 */
function validateLeaveRequestForm(startDate, endDate, reason, type) {
    // Check required fields
    if (!startDate) {
        showNotification('Please select a start date', 'error');
        return false;
    }
    
    if (!endDate) {
        showNotification('Please select an end date', 'error');
        return false;
    }
    
    if (!type) {
        showNotification('Please select a leave type', 'error');
        return false;
    }
    
    // Validate dates
    const start = new Date(startDate);
    const end = new Date(endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (start <= today) {
        showNotification('Start date must be in the future', 'error');
        return false;
    }
    
    if (end < start) {
        showNotification('End date must be after start date', 'error');
        return false;
    }
    
    // Check leave duration (max 30 days)
    const daysDifference = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    if (daysDifference > 30) {
        showNotification('Leave duration cannot exceed 30 days', 'error');
        return false;
    }
    
    // Validate reason length
    if (reason && reason.trim().length > 500) {
        showNotification('Reason cannot exceed 500 characters', 'error');
        return false;
    }
    
    return true;
}

/**
 * Validate date inputs when they change
 */
function validateLeaveDates() {
    const startDateInput = document.getElementById('leaveStartDate');
    const endDateInput = document.getElementById('leaveEndDate');
    
    const startDate = startDateInput.value;
    const endDate = endDateInput.value;
    
    if (startDate) {
        // Set end date minimum to start date
        endDateInput.min = startDate;
        
        // If end date is before start date, clear it
        if (endDate && new Date(endDate) < new Date(startDate)) {
            endDateInput.value = '';
        }
    }
}

/**
 * Load current employee's leave requests
 */
async function loadMyLeaveRequests() {
    try {
        const response = await fetchWithAuth('/api/v1/leave-requests/my-requests');
        
        if (response.ok) {
            const result = await response.json();
            // Extract the data array from the API response
            const requests = result.data || [];
            leaveRequests = requests;
            displayMyLeaveRequests(requests);
        } else {
            console.error('Failed to load leave requests');
            displayMyLeaveRequests([]);
        }
        
    } catch (error) {
        console.error('Error loading leave requests:', error);
        displayMyLeaveRequests([]);
    }
}

/**
 * Load pending leave requests for admin approval
 */
async function loadPendingLeaveRequests() {
    try {
        const response = await fetchWithAuth('/api/v1/leave-requests/pending');
        
        if (response.ok) {
            const result = await response.json();
            // Extract the data array from the API response
            const requests = result.data || [];
            displayPendingLeaveRequests(requests);
        } else {
            console.error('Failed to load pending requests');
            displayPendingLeaveRequests([]);
        }
        
    } catch (error) {
        console.error('Error loading pending requests:', error);
        displayPendingLeaveRequests([]);
    }
}

/**
 * Display employee's leave requests in the table
 */
function displayMyLeaveRequests(requests) {
    const tableBody = document.querySelector('#myLeaveRequestsTable tbody');
    if (!tableBody) return;
    
    tableBody.innerHTML = '';
    
    if (requests.length === 0) {
        const row = tableBody.insertRow();
        const cell = row.insertCell(0);
        cell.colSpan = 6;
        cell.className = 'text-center';
        cell.textContent = 'No leave requests found';
        return;
    }
    
    requests.forEach(request => {
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
        if (request.status === 'PENDING') {
            actionsCell.innerHTML = `
                <button class="btn btn-sm btn-outline-danger" onclick="cancelLeaveRequest(${request.id})">
                    Cancel
                </button>
            `;
        } else {
            actionsCell.textContent = '-';
        }
    });
}

/**
 * Display pending leave requests for admin approval
 */
function displayPendingLeaveRequests(requests) {
    const tableBody = document.querySelector('#pendingLeaveRequestsTable tbody');
    if (!tableBody) return;
    
    tableBody.innerHTML = '';
    
    if (requests.length === 0) {
        const row = tableBody.insertRow();
        const cell = row.insertCell(0);
        cell.colSpan = 7;
        cell.className = 'text-center';
        cell.textContent = 'No pending requests';
        return;
    }
    
    requests.forEach(request => {
        const row = tableBody.insertRow();
        
        // Employee
        const employeeCell = row.insertCell(0);
        employeeCell.textContent = request.employeeName || `Employee ${request.employeeId}`;
        
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
        reasonCell.textContent = request.reason || '-';
        reasonCell.title = request.reason || '';
        
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
 * Cancel a leave request
 */
async function cancelLeaveRequest(requestId) {
    if (!confirm('Are you sure you want to cancel this leave request?')) {
        return;
    }
    
    try {
        const response = await fetchWithAuth(`/api/v1/leave-requests/${requestId}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showNotification('Leave request cancelled successfully', 'success');
            loadMyLeaveRequests(); // Refresh the list
        } else {
            showNotification(result.message || 'Failed to cancel leave request', 'error');
        }
        
    } catch (error) {
        console.error('Error cancelling leave request:', error);
        showNotification('Network error. Please try again.', 'error');
    }
}

/**
 * Approve a leave request (admin only)
 */
async function approveLeaveRequest(requestId) {
    if (!confirm('Are you sure you want to approve this leave request?')) {
        return;
    }
    
    try {
        const response = await fetchWithAuth(`/api/v1/leave-requests/${requestId}/approve`, {
            method: 'PUT'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showNotification('Leave request approved successfully', 'success');
            loadPendingLeaveRequests(); // Refresh pending list
            loadMyLeaveRequests(); // Also refresh personal list if needed
        } else {
            showNotification(result.message || 'Failed to approve leave request', 'error');
        }
        
    } catch (error) {
        console.error('Error approving leave request:', error);
        showNotification('Network error. Please try again.', 'error');
    }
}

/**
 * Reject a leave request (admin only)
 */
async function rejectLeaveRequest(requestId) {
    const reason = prompt('Please enter the reason for rejection:');
    if (!reason || reason.trim() === '') {
        return;
    }
    
    try {
        const response = await fetchWithAuth(`/api/v1/leave-requests/${requestId}/reject`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(reason.trim())
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showNotification('Leave request rejected', 'success');
            loadPendingLeaveRequests(); // Refresh pending list
        } else {
            showNotification(result.message || 'Failed to reject leave request', 'error');
        }
        
    } catch (error) {
        console.error('Error rejecting leave request:', error);
        showNotification('Network error. Please try again.', 'error');
    }
}

/**
 * Reset the leave request form
 */
function resetLeaveRequestForm() {
    document.getElementById('leaveStartDate').value = '';
    document.getElementById('leaveEndDate').value = '';
    document.getElementById('leaveReason').value = '';
    document.getElementById('leaveType').selectedIndex = 0;
}

/**
 * Utility functions
 */

function formatLeaveType(type) {
    const types = {
        'VACATION': 'Vacation',
        'SICK': 'Sick Leave',
        'PERSONAL': 'Personal Leave',
        'MATERNITY': 'Maternity Leave',
        'PATERNITY': 'Paternity Leave',
        'BEREAVEMENT': 'Bereavement Leave',
        'EMERGENCY': 'Emergency Leave',
        'STUDY': 'Study Leave',
        'UNPAID': 'Unpaid Leave'
    };
    return types[type] || type;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function calculateLeaveDuration(startDate, endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
}

function isCurrentUserAdmin() {
    // Check if current user has admin role
    const currentUser = window.currentUser || {};
    return currentUser.role === 'ADMIN';
}

function showNotification(message, type) {
    // Use existing notification system or create a simple one
    if (typeof showMessage === 'function') {
        showMessage(message, type);
    } else {
        alert(message);
    }
}

// Make functions globally available
window.cancelLeaveRequest = cancelLeaveRequest;
window.approveLeaveRequest = approveLeaveRequest;
window.rejectLeaveRequest = rejectLeaveRequest;
