async function openReservationPage(roomId) {
  const listRoom = currentRooms.find((item) => Number(item.id) === Number(roomId));
  const room = await fetchRoomDetails(roomId, listRoom);
  if (!room) {
    return;
  }

  const checkIn = document.getElementById("checkInDate").value;
  const checkOut = document.getElementById("checkOutDate").value;
  const adults = Number(document.getElementById("adults").value);
  const children = Number(document.getElementById("children").value);

  if (!checkIn || !checkOut) {
    alert("Please select your check-in and check-out dates first");
    return;
  }

  const nights = Math.ceil(
    (new Date(checkOut) - new Date(checkIn)) / (1000 * 60 * 60 * 24),
  );

  if (nights <= 0) {
    alert("The departure date must be after the arrival date");
    return;
  }

  const price = getRoomPrice(room);
  const totalPrice = price * nights;
  const roomName = getRoomDisplayName(room);

  document.getElementById("bookingContent").innerHTML = `
    <div class="booking-scroll">
      <article class="booking-detail-card">
        <h1 class="booking-title">${escapeHtml(roomName)}</h1>
        <div class="booking-room-layout">
          <div class="booking-media">
            <img
              class="booking-room-image"
              src="${escapeAttribute(getRoomImage(room))}"
              alt="${escapeAttribute(roomName)}"
              loading="lazy"
              decoding="async"
            />
            <div class="booking-price">
              ${createRatingMarkup(4.5)}
              <strong>${formatCurrency(price)}</strong>
            </div>
          </div>
          <div class="booking-copy">
            <p>${escapeHtml(room.description || "Comfortable room with all essential amenities.")}</p>
            <button
              class="booking-action"
              type="button"
              onclick="bookRoomFromPage(this, ${Number(roomId)}, '${checkIn}', '${checkOut}', ${adults}, ${children}, ${totalPrice})">
              Book
            </button>
          </div>
        </div>

        <div class="feedback-board" id="bookingFeedback">
          <div class="feedback-empty">Loading reviews...</div>
        </div>
      </article>
    </div>
  `;

  showDashboardSection("booking");
  loadBookingFeedback(6);
}

async function fetchRoomDetails(roomId, fallbackRoom) {
  try {
    const response = await fetch(`/api/public/rooms/${roomId}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to load room details");
    }

    const room = await response.json();
    return { ...(fallbackRoom || {}), ...room };
  } catch (error) {
    console.error("Error loading room details:", error);
    return fallbackRoom;
  }
}

function bookRoomFromPage(button, roomId, checkIn, checkOut, adults, children, totalPrice) {
  const originalText = button.textContent;
  button.textContent = "Booking...";
  button.disabled = true;

  window.setTimeout(() => {
    showReservationConfirmation();
    button.textContent = originalText;
    button.disabled = false;
  }, 650);
}

async function loadBookingFeedback(limit) {
  const feedbackContainer = document.getElementById("bookingFeedback");
  if (!feedbackContainer) {
    return;
  }

  const reviews = await fetchLatestReviews(limit);
  feedbackContainer.innerHTML = createFeedbackMarkup(reviews);
}

async function fetchLatestReviews(limit) {
  try {
    const token = localStorage.getItem("jwtToken");
    const headers = {
      "Content-Type": "application/json",
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`/api/client/reviews/latest?limit=${limit}`, {
      method: "GET",
      headers,
    });

    if (!response.ok) {
      throw new Error("Failed to load reviews");
    }

    return await response.json();
  } catch (error) {
    console.error("Error loading reviews:", error);
    return [];
  }
}

function createFeedbackMarkup(reviews) {
  if (!reviews || reviews.length === 0) {
    return `
      <div class="feedback-empty">
        No validated reviews at the moment.
      </div>
    `;
  }

  return reviews
    .slice(0, 4)
    .map((review, index) => createFeedbackCard(review, index))
    .join("");
}

function createFeedbackCard(review, index) {
  const alignClass = index % 2 === 0 ? "feedback-item left" : "feedback-item right";
  const rating = Number(review.rating || 5);

  return `
    <div class="${alignClass}">
      <div class="feedback-avatar" aria-hidden="true">
        <span>${escapeHtml(getInitials(review.authorName || "Client"))}</span>
      </div>
      <div>
        ${createRatingMarkup(rating)}
        <p>${escapeHtml(review.comment || "Excellent stay!")}</p>
      </div>
    </div>
  `;
}

function createRatingMarkup(rating) {
  const roundedRating = Math.max(0, Math.min(5, Math.round(Number(rating || 0))));
  const stars = Array.from({ length: 5 }, (_, index) => (
    `<span class="${index < roundedRating ? "filled" : ""}"></span>`
  )).join("");

  return `<span class="rating-stars" aria-label="${roundedRating}/5">${stars}</span>`;
}

function getInitials(name) {
  return String(name)
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("") || "C";
}
