// ================================
// Client Home Page JavaScript
// ================================

// Global variables
let currentRooms = [];
let currentReviews = [];
let reviewsOffset = 0;
const reviewsLimit = 6;

// Initialize page when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  initializePage();
  loadRooms();
  loadReviews();
  setMinDate();
});

// Initialize page functionality
function initializePage() {
  // Smooth scrolling for navigation links
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      e.preventDefault();
      const target = document.querySelector(this.getAttribute("href"));
      if (target) {
        target.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      }
    });
  });

  // Set default dates
  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const nextDay = new Date(tomorrow);
  nextDay.setDate(nextDay.getDate() + 1);

  document.getElementById("checkInDate").value = tomorrow
    .toISOString()
    .split("T")[0];
  document.getElementById("checkOutDate").value = nextDay
    .toISOString()
    .split("T")[0];
}

// Set minimum date for date inputs
function setMinDate() {
  const today = new Date().toISOString().split("T")[0];
  document.getElementById("checkInDate").min = today;
  document.getElementById("checkOutDate").min = today;

  // Update checkout minimum when checkin changes
  document
    .getElementById("checkInDate")
    .addEventListener("change", function () {
      const checkinDate = new Date(this.value);
      const checkoutDate = new Date(checkinDate);
      checkoutDate.setDate(checkoutDate.getDate() + 1);

      document.getElementById("checkOutDate").min = checkoutDate
        .toISOString()
        .split("T")[0];

      if (
        new Date(document.getElementById("checkOutDate").value) <= checkinDate
      ) {
        document.getElementById("checkOutDate").value = checkoutDate
          .toISOString()
          .split("T")[0];
      }
    });
}

// Load and display rooms
async function loadRooms() {
  showLoading("roomsContainer");

  try {
    const response = await fetch("/api/public/rooms", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to load rooms");
    }

    const rooms = await response.json();
    displayRooms(rooms);
    currentRooms = rooms;
  } catch (error) {
    console.error("Error loading rooms:", error);
    // Fallback to sample data if API fails
    console.log("Falling back to sample data...");
    const sampleRooms = getSampleRooms();
    displayRooms(sampleRooms);
    currentRooms = sampleRooms;
  }
}

// Search rooms based on criteria
async function searchRooms() {
  const checkIn = document.getElementById("checkInDate").value;
  const checkOut = document.getElementById("checkOutDate").value;
  const adults = parseInt(document.getElementById("adults").value);
  const children = parseInt(document.getElementById("children").value);
  const totalGuests = adults + children;

  if (!checkIn || !checkOut) {
    alert("Please select your check-in and check-out dates first");
    return;
  }

  if (new Date(checkIn) >= new Date(checkOut)) {
    alert("The departure date must be after the arrival date");
    return;
  }

  showLoading("roomsContainer");

  try {
    // For now, we'll filter client-side based on capacity
    // In the future, this can be enhanced with a proper availability API
    const response = await fetch("/api/public/rooms", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to search rooms");
    }

    const allRooms = await response.json();

    // Filter rooms by capacity
    const filteredRooms = allRooms.filter(
      (room) => room.capacity >= totalGuests,
    );

    displayRooms(filteredRooms);
  } catch (error) {
    console.error("Error searching rooms:", error);
    // Fallback to sample data filtering
    const sampleRooms = getSampleRooms();
    const filteredRooms = sampleRooms.filter(
      (room) => room.maxOccupancy >= totalGuests,
    );
    displayRooms(filteredRooms);
  }
}

// Display rooms in the container
function displayRooms(rooms) {
  const container = document.getElementById("roomsContainer");

  if (rooms.length === 0) {
    container.innerHTML = `
            <div class="col-12 text-center">
                <div class="alert alert-info">
                    <i class="fas fa-info-circle"></i>
                    No rooms available for the selected criteria.
                </div>
            </div>
        `;
    return;
  }

  container.innerHTML = rooms
    .map(
      (room) => `
        <div class="col-lg-4 col-md-6">
            <div class="card room-card">
                <div class="room-image" style="background-image: url('${room.imageUrl || "/image/logo.png"}')">
                    <span class="room-badge">${getRoomDisplayName(room)}</span>
                </div>
                <div class="card-body">
                    <h5 class="card-title">${getRoomDisplayName(room)}</h5>
                    <p class="card-text">${room.description || "Comfortable room with all modern amenities."}</p>

                    <ul class="room-features">
                        <li><i class="fas fa-users"></i> ${room.capacity} max guests</li>
                        ${
                          room.amenities && room.amenities.length > 0
                            ? room.amenities
                                .slice(0, 4)
                                .map((amenity) => {
                                  const icon = getAmenityIcon(amenity);
                                  return `<li><i class="${icon}"></i> ${amenity}</li>`;
                                })
                                .join("")
                            : `<li><i class="fas fa-wifi"></i> Free WiFi</li>
                             <li><i class="fas fa-tv"></i> Television</li>
                             <li><i class="fas fa-snowflake"></i> Air Conditioning</li>`
                        }
                    </ul>

                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <div class="room-price">
                            EUR ${room.price || 129}
                            <small>/night</small>
                        </div>
                        <span class="availability-status ${room.status === "AVAILABLE" ? "available" : "unavailable"}">
                            ${getRoomStatusText(room.status)}
                        </span>
                    </div>

                    <div class="mt-3">
                        <button class="btn btn-primary w-100" onclick="openReservationModal(${room.id})"
                                ${room.status !== "AVAILABLE" ? "disabled" : ""}>
                            <i class="fas fa-calendar-check"></i>
                            ${room.status !== "AVAILABLE" ? "Not available" : "Reserve"}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `,
    )
    .join("");
}

// Get room display name (number + name if available, otherwise just number with type)
function getRoomDisplayName(room) {
  if (room.name && room.name.trim()) {
    return `${room.number} - ${room.name}`;
  }
  // Convert enum type to display name
  const typeNames = {
    STANDARD: "Standard Room",
    SUPERIOR: "Superior Room",
    DELUXE: "Deluxe Room",
    JUNIOR_SUITE: "Junior Suite",
    SUITE: "Suite",
    PRESIDENTIAL_SUITE: "Presidential Suite",
    FAMILY_ROOM: "Family Room",
    TWIN: "Twin Room",
    DOUBLE: "Double Room",
    SINGLE: "Single Room",
  };
  return `${room.number} - ${typeNames[room.type] || room.type}`;
}

// Get room status text
function getRoomStatusText(status) {
  switch (status) {
    case "AVAILABLE":
      return "Available";
    case "OCCUPIED":
      return "Occupied";
    case "MAINTENANCE":
      return "Maintenance";
    case "OUT_OF_ORDER":
      return "Out of Order";
    default:
      return "Available";
  }
}

// Get appropriate icon for amenity
function getAmenityIcon(amenity) {
  const amenityLower = amenity.toLowerCase();

  if (amenityLower.includes("wifi")) return "fas fa-wifi";
  if (amenityLower.includes("balcony")) return "fas fa-leaf";
  if (amenityLower.includes("jacuzzi")) return "fas fa-hot-tub";
  if (amenityLower.includes("view")) return "fas fa-mountain";
  if (amenityLower.includes("mini-bar") || amenityLower.includes("minibar"))
    return "fas fa-wine-glass";
  if (amenityLower.includes("television") || amenityLower.includes("tv"))
    return "fas fa-tv";
  if (amenityLower.includes("room service")) return "fas fa-bell";
  if (amenityLower.includes("safe")) return "fas fa-lock";
  if (amenityLower.includes("dryer") || amenityLower.includes("hair"))
    return "fas fa-wind";
  if (amenityLower.includes("bathrobe")) return "fas fa-tshirt";
  if (amenityLower.includes("coffee") || amenityLower.includes("nespresso"))
    return "fas fa-coffee";
  if (amenityLower.includes("champagne") || amenityLower.includes("bottle"))
    return "fas fa-champagne-glasses";
  if (amenityLower.includes("spa")) return "fas fa-spa";
  if (amenityLower.includes("living")) return "fas fa-couch";
  if (amenityLower.includes("fridge")) return "fas fa-snowflake";
  if (amenityLower.includes("games") || amenityLower.includes("child"))
    return "fas fa-gamepad";
  if (amenityLower.includes("desk")) return "fas fa-desk";

  // Default icon
  return "fas fa-star";
}

// Get availability text
function getAvailabilityText(availability) {
  switch (availability) {
    case "available":
      return "Available";
    case "limited":
      return "Last Rooms";
    case "unavailable":
      return "Full";
    default:
      return "Available";
  }
}

// Load and display reviews
async function loadReviews() {
  showLoading("reviewsContainer");

  try {
    const response = await fetch("/api/client/reviews/latest?limit=6", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to load reviews");
    }

    const reviews = await response.json();
    displayReviews(reviews);
    currentReviews = reviews;
  } catch (error) {
    console.error("Error loading reviews:", error);
    document.getElementById("reviewsContainer").innerHTML = `
            <div class="col-12 text-center">
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle"></i>
                    <h5>Loading Error</h5>
                    <p class="mb-0">Unable to load reviews at the moment. Please try again later.</p>
                </div>
            </div>
        `;
  }
}

// Load more reviews
async function loadMoreReviews() {
  reviewsOffset += reviewsLimit;

  try {
    const response = await fetch(
      `/api/client/reviews?offset=${reviewsOffset}&limit=${reviewsLimit}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
          "Content-Type": "application/json",
        },
      },
    );

    if (!response.ok) {
      throw new Error("Failed to load more reviews");
    }

    const moreReviews = await response.json();
    if (moreReviews.length > 0) {
      appendReviews(moreReviews);
    } else {
      // Hide the "Load More" button if no more reviews
      const loadMoreBtn = document.querySelector(
        'button[onclick="loadMoreReviews()"]',
      );
      if (loadMoreBtn) {
        loadMoreBtn.style.display = "none";
      }
    }
  } catch (error) {
    console.error("Error loading more reviews:", error);
  }
}

// Display reviews
function displayReviews(reviews) {
  const container = document.getElementById("reviewsContainer");

  if (!reviews || reviews.length === 0) {
    container.innerHTML = `
            <div class="col-12 text-center">
                <div class="alert alert-info">
                    <i class="fas fa-info-circle"></i>
                    <h5>No validated reviews at the moment</h5>
                    <p class="mb-0">The first reviews from our clients will appear here once validated by our team.</p>
                </div>
            </div>
        `;
    return;
  }

  container.innerHTML = reviews
    .slice(0, reviewsLimit)
    .map((review) => createReviewCard(review))
    .join("");
}

// Append more reviews
function appendReviews(reviews) {
  const container = document.getElementById("reviewsContainer");
  container.innerHTML += reviews
    .map((review) => createReviewCard(review))
    .join("");
}

// Create review card HTML
function createReviewCard(review) {
  return `
        <div class="col-lg-4 col-md-6">
            <div class="review-card fade-in-up">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <div class="review-stars">
                        ${generateStars(review.rating)}
                    </div>
                    <span class="badge bg-success">
                        <i class="fas fa-check-circle"></i> Validated
                    </span>
                </div>
                <p class="review-text">"${review.comment || "Excellent stay!"}"</p>
                <div class="review-author">
                    <strong>${review.authorName || "Client"}</strong>
                    ${review.roomName ? `<small class="text-muted"> - ${review.roomName}</small>` : ""}
                </div>
                <div class="review-date">${formatDate(review.reviewDate || review.createdAt)}</div>
            </div>
        </div>
    `;
}

// Generate star rating HTML
function generateStars(rating) {
  let stars = "";
  for (let i = 1; i <= 5; i++) {
    if (i <= rating) {
      stars += '<i class="fas fa-star"></i>';
    } else if (i - 0.5 <= rating) {
      stars += '<i class="fas fa-star-half-alt"></i>';
    } else {
      stars += '<i class="far fa-star"></i>';
    }
  }
  return stars;
}

// Format date for display
function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString("fr-FR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

// Open reservation modal
function openReservationModal(roomId) {
  const room = currentRooms.find((r) => r.id === roomId);
  if (!room) return;

  const checkIn = document.getElementById("checkInDate").value;
  const checkOut = document.getElementById("checkOutDate").value;
  const adults = document.getElementById("adults").value;
  const children = document.getElementById("children").value;

  if (!checkIn || !checkOut) {
    alert("Please select your check-in and check-out dates first");
    return;
  }

  // Calculate number of nights and total price
  const nights = Math.ceil(
    (new Date(checkOut) - new Date(checkIn)) / (1000 * 60 * 60 * 24),
  );
  const totalPrice = room.price * nights;

  const modalContent = `
        <div class="row">
            <div class="col-md-6">
                <img src="${room.imageUrl}" alt="${room.name}" class="img-fluid rounded">
            </div>
            <div class="col-md-6">
                <h4>${room.name}</h4>
                <p>${room.description}</p>

                <div class="reservation-details">
                    <div class="row mb-2">
                        <div class="col-6"><strong>Check-in:</strong></div>
                        <div class="col-6">${formatDate(checkIn)}</div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-6"><strong>Check-out:</strong></div>
                        <div class="col-6">${formatDate(checkOut)}</div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-6"><strong>Length:</strong></div>
                        <div class="col-6">${nights} night${nights > 1 ? "s" : ""}</div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-6"><strong>Occupants:</strong></div>
                        <div class="col-6">${adults} adult${adults > 1 ? "s" : ""}${children > 0 ? `, ${children} child${children > 1 ? "ren" : ""}` : ""}</div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-6"><strong>Price per night:</strong></div>
                        <div class="col-6">EUR ${room.price}</div>
                    </div>
                    <hr>
                    <div class="row mb-3">
                        <div class="col-6"><strong>Total:</strong></div>
                        <div class="col-6"><strong class="text-primary">EUR ${totalPrice}</strong></div>
                    </div>
                </div>

                <div class="payment-options">
                    <div class="payment-option" onclick="selectPaymentOption('now')">
                        <i class="fas fa-credit-card"></i>
                        <div><strong>Pay now</strong></div>
                        <small>Immediate secure payment</small>
                    </div>
                    <div class="payment-option" onclick="selectPaymentOption('later')">
                        <i class="fas fa-clock"></i>
                        <div><strong>Pay later</strong></div>
                        <small>Reservation without immediate payment</small>
                    </div>
                </div>

                <div class="mt-3">
                    <button class="btn btn-primary w-100" onclick="processReservation(${roomId}, '${checkIn}', '${checkOut}', ${adults}, ${children}, ${totalPrice})">
                        <i class="fas fa-check"></i> Confirm reservation
                    </button>
                </div>
            </div>
        </div>
    `;

  document.getElementById("reservationForm").innerHTML = modalContent;
  const modal = new bootstrap.Modal(
    document.getElementById("reservationModal"),
  );
  modal.show();
}

// Select payment option
function selectPaymentOption(option) {
  document.querySelectorAll(".payment-option").forEach((el) => {
    el.classList.remove("selected");
  });
  event.target.closest(".payment-option").classList.add("selected");
}

// Process reservation
function processReservation(
  roomId,
  checkIn,
  checkOut,
  adults,
  children,
  totalPrice,
) {
  const selectedPayment = document.querySelector(".payment-option.selected");
  if (!selectedPayment) {
    alert("Please select a payment option");
    return;
  }

  const paymentOption = selectedPayment.onclick.toString().includes("now")
    ? "now"
    : "later";

  // Show loading state
  const button = event.target;
  const originalText = button.innerHTML;
  button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
  button.disabled = true;

  // Simulate reservation process
  setTimeout(() => {
    if (paymentOption === "now") {
      // Redirect to payment page or open payment modal
      alert("Redirecting to secure payment...");
      // window.location.href = '/payment?reservation=' + btoa(JSON.stringify({roomId, checkIn, checkOut, adults, children, totalPrice}));
    } else {
      // Show confirmation message
      showReservationConfirmation();
    }

    // Close modal
    bootstrap.Modal.getInstance(
      document.getElementById("reservationModal"),
    ).hide();

    // Reset button
    button.innerHTML = originalText;
    button.disabled = false;
  }, 2000);
}

// Show reservation confirmation
function showReservationConfirmation() {
  const alertHtml = `
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="fas fa-check-circle"></i>
            <strong>Reservation confirmed!</strong> You will receive a confirmation email shortly.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;

  document.body.insertAdjacentHTML("afterbegin", alertHtml);

  // Scroll to top to show the alert
  window.scrollTo({ top: 0, behavior: "smooth" });

  // Auto-remove alert after 5 seconds
  setTimeout(() => {
    const alert = document.querySelector(".alert-success");
    if (alert) {
      alert.remove();
    }
  }, 5000);
}

// Show loading spinner
function showLoading(containerId) {
  const container = document.getElementById(containerId);
  container.innerHTML = `
        <div class="col-12">
            <div class="loading">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
                <p class="mt-3">Loading...</p>
            </div>
        </div>
    `;
}

// Sample data functions (replace with actual API calls)
function getSampleRooms() {
  return [
    {
      id: 1,
      name: "Deluxe Room",
      type: "DELUXE",
      description:
        "Spacious room with mountain view, perfect for a romantic getaway.",
      price: 189,
      maxOccupancy: 2,
      bedType: "King-size bed",
      size: 35,
      imageUrl:
        "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3",
      availability: "available",
      hasBalcony: true,
      hasJacuzzi: false,
    },
    {
      id: 2,
      name: "President Suite",
      type: "SUITE",
      description:
Y        "Our most luxurious suite with separate living room, jacuzzi, and panoramic views.",
      price: 450,
      maxOccupancy: 4,
      bedType: "King-size bed + sofa-bed",
      size: 85,
      imageUrl:
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3",
      availability: "limited",
      hasBalcony: true,
      hasJacuzzi: true,
    },
    {
      id: 3,
      name: "Standard Room",
      type: "STANDARD",
      description:
        "Room with all essential amenities for a comfortable stay.",
      price: 129,
      maxOccupancy: 2,
      bedType: "King-size bed",
      size: 25,
      imageUrl:
        "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?ixlib=rb-4.0.3",
      availability: "available",
      hasBalcony: false,
      hasJacuzzi: false,
    },
    {
      id: 4,
      name: "Family Room",
      type: "FAMILY",
      description:
        "Spacious family room with loft beds, perfect for families.",
      price: 229,
      maxOccupancy: 6,
      bedType: "King-size bed + sofa-bed",
      size: 45,
      imageUrl:
        "https://images.unsplash.com/photo-1560472355-536de3962603?ixlib=rb-4.0.3",
      availability: "available",
      hasBalcony: true,
      hasJacuzzi: false,
    },
    {
      id: 5,
      name: "Superior Room",
      type: "SUPERIOR",
      description:
        "Elegant room with refined decoration and modern amenities.",
      price: 159,
      maxOccupancy: 2,
      bedType: "Queen-size bed",
      size: 30,
      imageUrl:
        "https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3",
      availability: "unavailable",
      hasBalcony: false,
      hasJacuzzi: false,
    },
    {
      id: 6,
      name: "Junior Suite",
      type: "JUNIOR_SUITE",
      description: "Junior suite with living area and breathtaking view of the valley.",
      price: 289,
      maxOccupancy: 3,
      bedType: "King-size bed + sofa-bed",
      size: 55,
      imageUrl:
        "https://images.unsplash.com/photo-1587985064135-0366536eab42?ixlib=rb-4.0.3",
      availability: "limited",
      hasBalcony: true,
      hasJacuzzi: true,
    },
  ];
}

function getSampleReviews(offset = 0) {
  const allReviews = [
    {
      id: 1,
      clientName: "Marie Dubois",
      rating: 5,
      comment:
        "An absolutely magical stay! The hotel is sumptuous and the service impeccable. The view from our room was breathtaking.",
      date: "2024-12-15",
      stayDuration: "3 nights",
      status: "VALIDATED",
    },
    {
      id: 2,
      clientName: "Jean-Pierre Martin",
      rating: 4.5,
      comment:
        "Excellent hotel with an exceptional setting. The spa is fantastic and the restaurant offers refined cuisine. We will definitely come back!",
      date: "2024-12-10",
      stayDuration: "5 nights",
      status: "VALIDATED",
    },
    {
      id: 3,
      clientName: "Sophie Laurent",
      rating: 5,
      comment:
        "A perfect weekend in an idyllic setting. The welcome was warm and our suite was magnificent. Thank you for these unforgettable moments.",
      date: "2024-12-08",
      stayDuration: "2 nights",
      status: "VALIDATED",
    },
    {
      id: 4,
      clientName: "Paul Bernaud",
      rating: 4,
      comment:
        "A very beautiful hotel with impressive architecture. The rooms are spacious and well equipped. The only downside: the wifi was a bit slow.",
      date: "2024-12-05",
      stayDuration: "4 nights",
      status: "VALIDATED",
    },
    {
      id: 5,
      clientName: "Catherine Moreau",
      rating: 5,
      comment:
        "The Overlook Hotel exceeds all expectations! The personalized service, the quality of the amenities, and the beauty of the place make it a unique destination.",
      date: "2024-12-01",
      stayDuration: "1 semaine",
      status: "VALIDATED",
    },
    {
      id: 6,
      clientName: "Michel Rousseau",
      rating: 4.5,
      comment:
        "A high-class hotel in an exceptional environment. Breakfast was delicious and the staff very attentive.",
      date: "2024-11-28",
      stayDuration: "3 nights",
      status: "VALIDATED",
    },
  ];

  return allReviews.slice(offset, offset + reviewsLimit);
}
