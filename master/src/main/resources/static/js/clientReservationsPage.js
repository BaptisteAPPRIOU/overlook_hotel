// Client reservations dashboard page

let reservationsLoaded = false;
let activeReservation = null;

document.addEventListener("dashboard:section-change", function (event) {
  if (event.detail?.sectionId === "reservations") {
    loadClientReservations();
  }
});

document.addEventListener("DOMContentLoaded", function () {
  setupReservationModals();

  if (window.location.hash === "#reservations") {
    loadClientReservations();
  }
});

async function loadClientReservations(forceRefresh = false) {
  const container = document.getElementById("reservationsContainer");

  if (!container || (reservationsLoaded && !forceRefresh)) {
    return;
  }

  container.innerHTML = '<div class="reservations-loading">Loading reservations...</div>';

  try {
    const reservations = await fetchClientReservations();
    reservationsLoaded = true;
    renderClientReservations(reservations);
  } catch (error) {
    console.error("Error loading reservations:", error);
    container.innerHTML = `
      <div class="reservations-error">
        Unable to load reservations for now.
      </div>
    `;
  }
}

async function fetchClientReservations() {
  const token = localStorage.getItem("jwtToken");
  const headers = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch("/api/v1/clients/me/reservations", {
    method: "GET",
    headers,
  });

  if (!response.ok) {
    throw new Error("Failed to load reservations");
  }

  return response.json();
}

function renderClientReservations(reservations) {
  const container = document.getElementById("reservationsContainer");

  if (!container) {
    return;
  }

  if (!Array.isArray(reservations) || reservations.length === 0) {
    container.innerHTML = `
      <div class="reservations-empty">
        No reservations at the moment.
      </div>
    `;
    return;
  }

  container.innerHTML = reservations.map(createReservationRow).join("");
}

function createReservationRow(reservation) {
  const matchingRoom = findReservationRoom(reservation);
  const roomName = getReservationRoomName(reservation, matchingRoom);
  const dateLabel = formatReservationShortDate(
    reservation.reservationDateStart || reservation.startDate || reservation.checkIn,
  );
  const price = getReservationPrice(reservation, matchingRoom);
  const image = getReservationImage(reservation, matchingRoom);
  const reservationId = reservation.id || reservation.reservationId || "";

  return `
    <article class="reservation-row">
      <img
        class="reservation-thumb"
        src="${escapeAttribute(image)}"
        alt="${escapeAttribute(roomName)}"
        width="96"
        height="66"
        loading="lazy"
        decoding="async"
      />
      <h2 class="reservation-name">${escapeHtml(roomName)}</h2>
      <span class="reservation-date">${escapeHtml(dateLabel)}</span>
      <span class="reservation-price">${escapeHtml(formatCurrency(price))}</span>
      <div class="reservation-actions">
        <button
          class="reservation-icon-button"
          type="button"
          title="Cancel reservation"
          aria-label="Cancel reservation"
          data-reservation-action="delete"
          data-reservation-id="${escapeAttribute(reservationId)}"
          data-reservation-room="${escapeAttribute(roomName)}">
          ${getTrashIcon()}
        </button>
        <button
          class="reservation-icon-button"
          type="button"
          title="Leave feedback"
          aria-label="Leave feedback"
          data-reservation-action="feedback"
          data-reservation-id="${escapeAttribute(reservationId)}"
          data-reservation-room="${escapeAttribute(roomName)}">
          ${getPencilIcon()}
        </button>
      </div>
    </article>
  `;
}

function findReservationRoom(reservation) {
  const roomId = Number(reservation.roomId || reservation.room?.id);

  if (!roomId || !Array.isArray(currentRooms)) {
    return null;
  }

  return currentRooms.find((room) => Number(room.id) === roomId) || null;
}

function getReservationRoomName(reservation, matchingRoom) {
  if (matchingRoom) {
    return getRoomDisplayName(matchingRoom);
  }

  if (reservation.roomName) {
    return reservation.roomName;
  }

  if (reservation.roomType) {
    return reservation.roomType;
  }

  return "Room";
}

function getReservationPrice(reservation, matchingRoom) {
  return Number(
    reservation.totalAmount ||
      reservation.totalPrice ||
      reservation.price ||
      matchingRoom?.price ||
      matchingRoom?.basePrice ||
      0,
  );
}

function getReservationImage(reservation, matchingRoom) {
  return (
    reservation.roomImage ||
    reservation.imageUrl ||
    matchingRoom?.imageUrl ||
    "/image/logo_noBg_no_name.webp"
  );
}

function formatReservationShortDate(dateValue) {
  if (!dateValue) {
    return "--/--";
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return String(dateValue);
  }

  return date.toLocaleDateString("fr-FR", {
    day: "2-digit",
    month: "2-digit",
  });
}

function setupReservationModals() {
  document.addEventListener("click", function (event) {
    const actionButton = event.target.closest("[data-reservation-action]");
    if (actionButton) {
      activeReservation = {
        id: actionButton.dataset.reservationId || "",
        roomName: actionButton.dataset.reservationRoom || "this reservation",
      };

      if (actionButton.dataset.reservationAction === "delete") {
        prepareDeleteModal();
        openReservationModal("deleteReservationModal");
      }

      if (actionButton.dataset.reservationAction === "feedback") {
        prepareFeedbackModal();
        openReservationModal("feedbackReservationModal");
      }
    }

    if (event.target.closest("[data-reservation-modal-close]")) {
      closeReservationModals();
    }
  });

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
      closeReservationModals();
    }
  });

  document
    .getElementById("confirmDeleteReservation")
    ?.addEventListener("click", function () {
      closeReservationModals();
      notifyReservationAction("Delete reservation");
    });

  document
    .getElementById("sendReservationFeedback")
    ?.addEventListener("click", function () {
      closeReservationModals();
      notifyReservationAction("Send feedback");
    });
}

function openReservationModal(modalId) {
  const modal = document.getElementById(modalId);

  if (!modal) {
    return;
  }

  modal.classList.add("active");
  modal.setAttribute("aria-hidden", "false");
  document.body.classList.add("modal-open");
}

function closeReservationModals() {
  document.querySelectorAll(".reservation-modal.active").forEach((modal) => {
    modal.classList.remove("active");
    modal.setAttribute("aria-hidden", "true");
  });
  document.body.classList.remove("modal-open");
}

function prepareDeleteModal() {
  const title = document.getElementById("deleteReservationTitle");

  if (title) {
    title.textContent = `Delete reservation for ${activeReservation?.roomName || "this room"}?`;
  }
}

function prepareFeedbackModal() {
  const avatar = document.getElementById("feedbackReviewerAvatar");
  const clientName = document.getElementById("clientName")?.textContent || "";
  const feedbackText = document.getElementById("feedbackReservationText");

  if (avatar) {
    avatar.textContent = getReservationInitials(
      clientName || activeReservation?.roomName,
    );
  }

  if (feedbackText) {
    feedbackText.value = "";
  }
}

function getReservationInitials(value) {
  return String(value || "Client")
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

function notifyReservationAction(label) {
  const existingToast = document.querySelector(".toast-message");
  if (existingToast) {
    existingToast.remove();
  }

  document.body.insertAdjacentHTML(
    "afterbegin",
    `
      <div class="toast-message" role="status">
        <p><strong>${escapeHtml(label)}</strong> will be available when the back route is ready.</p>
        <button type="button" onclick="this.closest('.toast-message').remove()">OK</button>
      </div>
    `,
  );

  window.setTimeout(() => {
    document.querySelector(".toast-message")?.remove();
  }, 4200);
}

function getTrashIcon() {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"></path>
      <path d="M3 6h18"></path>
      <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
    </svg>
  `;
}

function getPencilIcon() {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M13 21h8"></path>
      <path d="m15 5 4 4"></path>
      <path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"></path>
    </svg>
  `;
}
