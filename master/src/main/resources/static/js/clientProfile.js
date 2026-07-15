// ================================
// Client Profile JavaScript
// ================================

// Global variables
let currentUser = null;
let userReservations = [];
let userReviews = [];
let currentFilter = "all";

// Initialize page when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  initializePage();
  setupProfileLogout();
  loadUserProfile();
  loadReservations();
  loadReviews();
  initializeStarRating();
});

// Initialize page functionality
function initializePage() {
  // Profile form submission
  document
    .getElementById("profileForm")
    .addEventListener("submit", function (e) {
      e.preventDefault();
      updateProfile();
    });

  // Review form setup
  setupReviewForm();
}

function setupProfileLogout() {
  document.querySelectorAll("[data-logout-link]").forEach((logoutLink) => {
    logoutLink.addEventListener("click", async function (event) {
      event.preventDefault();
      const token = localStorage.getItem("jwtToken");

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
  });
}

// Load user profile information
async function loadUserProfile() {
  try {
    // Fetch real user data from API
    const response = await fetch("/api/client/profile");
    if (!response.ok) {
      throw new Error("Failed to load profile");
    }

    currentUser = await response.json();
    displayUserProfile(currentUser);
  } catch (error) {
    console.error("Error loading profile:", error);

    // Fallback to mock data if API fails
    currentUser = {
      id: 1,
      firstName: "Jane",
      lastName: "Smith",
      email: "jane.smith@olh.fr",
      phone: "+33 4 90 04 96 77",
      address: "123 Rue de la Paix, 84000 Avignon, France",
    };

    displayUserProfile(currentUser);
    showErrorMessage("Using demo data");
  }
}

// Display user profile information
function displayUserProfile(user) {
  document.getElementById("userFullName").textContent =
    `${user.firstName} ${user.lastName}`;
  document.getElementById("userEmail").textContent = user.email;

  // Fill form fields
  document.getElementById("firstName").value = user.firstName;
  document.getElementById("lastName").value = user.lastName;
  document.getElementById("email").value = user.email;
  document.getElementById("phone").value = user.phone || "";
  document.getElementById("address").value = user.address || "";
}

// Load user reservations (mock data for demo)
async function loadReservations() {
  try {
    // Mock reservations data - in real app, fetch from API
    userReservations = [
      {
        id: 1,
        roomNumber: "301",
        roomType: "Presidential Suite",
        roomImage:
          "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
        checkIn: "2024-12-15",
        checkOut: "2024-12-18",
        guests: 2,
        totalPrice: 1350,
        status: "past",
        canReview: true,
        hasReview: false,
      },
      {
        id: 2,
        roomNumber: "201",
        roomType: "Deluxe Room",
        roomImage:
          "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
        checkIn: "2024-10-20",
        checkOut: "2024-10-23",
        guests: 2,
        totalPrice: 567,
        status: "past",
        canReview: true,
        hasReview: true,
      },
      {
        id: 3,
        roomNumber: "401",
        roomType: "Family Room",
        roomImage:
          "https://images.unsplash.com/photo-1560472355-536de3962603?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
        checkIn: "2025-08-15",
        checkOut: "2025-08-20",
        guests: 4,
        totalPrice: 1145,
        status: "confirmed",
        canReview: false,
        hasReview: false,
      },
      {
        id: 4,
        roomNumber: "102",
        roomType: "Standard Room",
        roomImage:
          "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
        checkIn: "2024-07-10",
        checkOut: "2024-07-12",
        guests: 2,
        totalPrice: 258,
        status: "cancelled",
        canReview: false,
        hasReview: false,
      },
    ];

    displayReservations(userReservations);
  } catch (error) {
    console.error("Error loading reservations:", error);
    showErrorMessage("Error loading reservations");
  }
}

// Display reservations
function displayReservations(reservations) {
  const container = document.getElementById("reservationsContainer");

  if (reservations.length === 0) {
    container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-calendar-times"></i>
                <h5>No reservations</h5>
                <p>You do not have any reservations yet.</p>
                <a href="/clientHomePage" class="btn btn-primary">
                    <i class="fas fa-plus"></i> Make a reservation
                </a>
            </div>
        `;
    return;
  }

  container.innerHTML = reservations
    .map(
      (reservation) => `
        <div class="reservation-card" data-status="${reservation.status}">
            <div class="row align-items-center">
                <div class="col-md-2">
                    <img src="${reservation.roomImage}" alt="${reservation.roomType}" class="room-image-small">
                </div>
                <div class="col-md-6">
                    <h6 class="mb-1">${reservation.roomNumber} - ${reservation.roomType}</h6>
                    <p class="text-muted mb-1">
                        <i class="fas fa-calendar"></i>
                        ${formatDate(reservation.checkIn)} - ${formatDate(reservation.checkOut)}
                    </p>
                    <p class="text-muted mb-0">
                        <i class="fas fa-users"></i> ${reservation.guests} guest(s)
                    </p>
                </div>
                <div class="col-md-2 text-center">
                    <div class="reservation-status status-${reservation.status}">
                        ${getStatusText(reservation.status)}
                    </div>
                </div>
                <div class="col-md-2 text-end">
                    <div class="mb-2">
                        <strong>EUR ${reservation.totalPrice}</strong>
                    </div>
                    <div class="review-actions">
                        ${
                          reservation.canReview && !reservation.hasReview
                            ? `<button class="btn btn-sm btn-warning" onclick="openReviewModal(${reservation.id})">
                                <i class="fas fa-star"></i> Leave a review
                            </button>`
                            : ""
                        }
                        ${
                          reservation.hasReview
                            ? `<span class="text-success"><i class="fas fa-check-circle"></i> Review submitted</span>`
                            : ""
                        }
                    </div>
                </div>
            </div>
        </div>
    `,
    )
    .join("");
}

// Load user reviews (mock data)
async function loadReviews() {
  try {
    // Mock reviews data
    userReviews = [
      {
        id: 1,
        reservationId: 2,
        roomNumber: "201",
        roomType: "Deluxe Room",
        rating: 5,
        comment:
          "An absolutely fantastic stay! The room was beautiful with a stunning view over the ochre cliffs. The service was exceptional and the staff was very attentive. I highly recommend it!",
        cleanliness: 5,
        service: 5,
        comfort: 5,
        valueForMoney: 4,
        recommend: true,
        anonymous: false,
        date: "2024-10-25",
        checkIn: "2024-10-20",
        checkOut: "2024-10-23",
      },
    ];

    displayReviews(userReviews);
  } catch (error) {
    console.error("Error loading reviews:", error);
    showErrorMessage("Error loading reviews");
  }
}

// Display reviews
function displayReviews(reviews) {
  const container = document.getElementById("reviewsContainer");

  if (reviews.length === 0) {
    container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-star"></i>
                <h5>No reviews</h5>
                <p>You have not left any reviews yet. After a stay, you will be able to share your experience.</p>
            </div>
        `;
    return;
  }

  container.innerHTML = reviews
    .map(
      (review) => `
        <div class="review-card">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                    <h6 class="mb-1">${review.roomNumber} - ${review.roomType}</h6>
                    <small class="text-muted">
                        Stay from ${formatDate(review.checkIn)} to ${formatDate(review.checkOut)}
                    </small>
                </div>
                <div class="text-end">
                    <div class="rating-display mb-1">
                        ${generateStarDisplay(review.rating)}
                    </div>
                    <small class="text-muted">${formatDate(review.date)}</small>
                </div>
            </div>

            <div class="review-details mb-3">
                <div class="row">
                    <div class="col-md-3">
                        <small class="text-muted">Cleanliness:</small>
                        <div>${generateStarDisplay(review.cleanliness)}</div>
                    </div>
                    <div class="col-md-3">
                        <small class="text-muted">Service:</small>
                        <div>${generateStarDisplay(review.service)}</div>
                    </div>
                    <div class="col-md-3">
                        <small class="text-muted">Comfort:</small>
                        <div>${generateStarDisplay(review.comfort)}</div>
                    </div>
                    <div class="col-md-3">
                        <small class="text-muted">Value:</small>
                        <div>${generateStarDisplay(review.valueForMoney)}</div>
                    </div>
                </div>
            </div>

            <p class="mb-3">${review.comment}</p>

            <div class="d-flex justify-content-between align-items-center">
                <div>
                    ${
                      review.recommend
                        ? '<span class="badge bg-success"><i class="fas fa-thumbs-up"></i> Recommended</span>'
                        : '<span class="badge bg-secondary"><i class="fas fa-thumbs-down"></i> Not recommended</span>'
                    }
                    ${
                      review.anonymous
                        ? '<span class="badge bg-info ms-2"><i class="fas fa-user-secret"></i> Anonymous</span>'
                        : ""
                    }
                </div>
                <button class="btn btn-sm btn-outline-primary" onclick="editReview(${review.id})">
                    <i class="fas fa-edit"></i> Edit
                </button>
            </div>
        </div>
    `,
    )
    .join("");
}

// Show specific section
function showSection(sectionId) {
  // Hide all sections
  document.querySelectorAll(".content-section").forEach((section) => {
    section.classList.remove("active");
  });

  // Remove active class from all menu items
  document.querySelectorAll(".menu-item").forEach((item) => {
    item.classList.remove("active");
  });

  // Show selected section
  document.getElementById(sectionId).classList.add("active");

  // Add active class to clicked menu item
  document.querySelector(`[href="#${sectionId}"]`).classList.add("active");
}

// Filter reservations
function filterReservations(filter) {
  currentFilter = filter;

  // Update button states
  document.querySelectorAll(".filter-buttons .btn").forEach((btn) => {
    btn.classList.remove("active");
  });
  event.target.classList.add("active");

  // Filter reservations
  let filteredReservations = userReservations;
  if (filter !== "all") {
    filteredReservations = userReservations.filter((r) => r.status === filter);
  }

  displayReservations(filteredReservations);
}

// Open review modal
function openReviewModal(reservationId) {
  const reservation = userReservations.find((r) => r.id === reservationId);
  if (!reservation) return;

  // Fill modal with reservation info
  document.getElementById("reservationId").value = reservationId;
  document.getElementById("reviewRoomInfo").textContent =
    `${reservation.roomNumber} - ${reservation.roomType}`;
  document.getElementById("reviewDateInfo").textContent =
    `${formatDate(reservation.checkIn)} - ${formatDate(reservation.checkOut)}`;

  // Reset form
  document.getElementById("reviewForm").reset();
  document.getElementById("rating").value = "";
  updateStarRating(0);

  // Show modal
  new bootstrap.Modal(document.getElementById("reviewModal")).show();
}

// Initialize star rating system
function initializeStarRating() {
  const stars = document.querySelectorAll("#starRating i");

  stars.forEach((star, index) => {
    star.addEventListener("click", () => {
      const rating = index + 1;
      document.getElementById("rating").value = rating;
      updateStarRating(rating);
    });

    star.addEventListener("mouseenter", () => {
      updateStarRating(index + 1, true);
    });
  });

  document.getElementById("starRating").addEventListener("mouseleave", () => {
    const currentRating = document.getElementById("rating").value || 0;
    updateStarRating(currentRating);
  });
}

// Update star rating display
function updateStarRating(rating, isHover = false) {
  const stars = document.querySelectorAll("#starRating i");

  stars.forEach((star, index) => {
    star.classList.remove("active");
    if (index < rating) {
      star.classList.add("active");
    }
  });
}

// Submit review
function submitReview() {
  const form = document.getElementById("reviewForm");
  const formData = new FormData(form);

  const reviewData = {
    reservationId: document.getElementById("reservationId").value,
    rating: document.getElementById("rating").value,
    cleanliness: document.getElementById("cleanliness").value,
    service: document.getElementById("service").value,
    comfort: document.getElementById("comfort").value,
    valueForMoney: document.getElementById("valueForMoney").value,
    comment: document.getElementById("reviewComment").value,
    recommend: document.getElementById("recommendHotel").checked,
    anonymous: document.getElementById("stayAnonymous").checked,
  };

  // Validation
  if (!reviewData.rating) {
    showErrorMessage("Please provide an overall rating");
    return;
  }

  if (!reviewData.comment || reviewData.comment.length < 10) {
    showErrorMessage("The comment must contain at least 10 characters");
    return;
  }

  // Simulate API call
  setTimeout(() => {
    // Update reservation status
    const reservation = userReservations.find(
      (r) => r.id == reviewData.reservationId,
    );
    if (reservation) {
      reservation.hasReview = true;
    }

    // Add to reviews (for demo purposes)
    const newReview = {
      id: Date.now(),
      reservationId: reviewData.reservationId,
      roomNumber: reservation.roomNumber,
      roomType: reservation.roomType,
      rating: parseInt(reviewData.rating),
      comment: reviewData.comment,
      cleanliness: parseInt(reviewData.cleanliness) || 0,
      service: parseInt(reviewData.service) || 0,
      comfort: parseInt(reviewData.comfort) || 0,
      valueForMoney: parseInt(reviewData.valueForMoney) || 0,
      recommend: reviewData.recommend,
      anonymous: reviewData.anonymous,
      date: new Date().toISOString().split("T")[0],
      checkIn: reservation.checkIn,
      checkOut: reservation.checkOut,
    };

    userReviews.unshift(newReview);

    // Refresh displays
    displayReservations(userReservations);
    displayReviews(userReviews);

    // Close modal and show success
    bootstrap.Modal.getInstance(document.getElementById("reviewModal")).hide();
    showSuccessMessage("Your review was posted successfully!");
  }, 1000);
}

// Update profile
function updateProfile() {
  const formData = {
    firstName: document.getElementById("firstName").value,
    lastName: document.getElementById("lastName").value,
    phone: document.getElementById("phone").value,
    address: document.getElementById("address").value,
  };

  // Simulate API call
  setTimeout(() => {
    // Update current user data
    Object.assign(currentUser, formData);

    // Update display
    document.getElementById("userFullName").textContent =
      `${formData.firstName} ${formData.lastName}`;

    showSuccessMessage("Profile updated successfully!");
  }, 1000);
}

// Reset profile form
function resetProfile() {
  displayUserProfile(currentUser);
}

// Edit review (for demo, just reopens modal with existing data)
function editReview(reviewId) {
  const review = userReviews.find((r) => r.id === reviewId);
  if (!review) return;

  // Find corresponding reservation
  const reservation = userReservations.find(
    (r) => r.id === review.reservationId,
  );
  if (!reservation) return;

  // Fill modal with existing review data
  document.getElementById("reservationId").value = review.reservationId;
  document.getElementById("reviewRoomInfo").textContent =
    `${review.roomNumber} - ${review.roomType}`;
  document.getElementById("reviewDateInfo").textContent =
    `${formatDate(review.checkIn)} - ${formatDate(review.checkOut)}`;

  // Fill form with existing data
  document.getElementById("rating").value = review.rating;
  document.getElementById("cleanliness").value = review.cleanliness || "";
  document.getElementById("service").value = review.service || "";
  document.getElementById("comfort").value = review.comfort || "";
  document.getElementById("valueForMoney").value = review.valueForMoney || "";
  document.getElementById("reviewComment").value = review.comment;
  document.getElementById("recommendHotel").checked = review.recommend;
  document.getElementById("stayAnonymous").checked = review.anonymous;

  updateStarRating(review.rating);

  // Show modal
  new bootstrap.Modal(document.getElementById("reviewModal")).show();
}

// Helper functions
function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString("fr-FR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function getStatusText(status) {
  const statusTexts = {
    confirmed: "Confirmed",
    past: "Completed",
    cancelled: "Cancelled",
    pending: "En attente",
  };
  return statusTexts[status] || status;
}

function generateStarDisplay(rating) {
  let stars = "";
  for (let i = 1; i <= 5; i++) {
    if (i <= rating) {
      stars += '<i class="fas fa-star"></i>';
    } else {
      stars += '<i class="fas fa-star empty"></i>';
    }
  }
  return stars;
}

function showSuccessMessage(message) {
  // Create toast or alert
  const toast = document.createElement("div");
  toast.className = "alert alert-success position-fixed";
  toast.style.cssText = "top: 100px; right: 20px; z-index: 9999;";
  toast.innerHTML = `
        <i class="fas fa-check-circle"></i> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 5000);
}

function showErrorMessage(message) {
  // Create toast or alert
  const toast = document.createElement("div");
  toast.className = "alert alert-danger position-fixed";
  toast.style.cssText = "top: 100px; right: 20px; z-index: 9999;";
  toast.innerHTML = `
        <i class="fas fa-exclamation-circle"></i> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 5000);
}

// Setup review form
function setupReviewForm() {
  // Add any additional form setup here
}
