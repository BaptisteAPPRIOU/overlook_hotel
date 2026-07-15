// Client dashboard home page

var currentRooms = [];

document.addEventListener("DOMContentLoaded", function () {
  initializePage();
  setMinDate();
  setupLogout();
  loadCurrentUser();
  loadRooms();
});

function initializePage() {
  setupDashboardNavigation();
  setupRoomFilters();
  setDefaultDates();
}

function setupDashboardNavigation() {
  document.querySelectorAll("[data-dashboard-target]").forEach((item) => {
    item.addEventListener("click", function () {
      showDashboardSection(this.dataset.dashboardTarget);
    });
  });

  const hashTarget = window.location.hash.replace("#", "");
  if (hashTarget && document.getElementById(hashTarget)) {
    showDashboardSection(hashTarget);
  }
}

function showDashboardSection(sectionId) {
  document.querySelectorAll("[data-dashboard-section]").forEach((section) => {
    section.classList.toggle("active", section.id === sectionId);
  });

  const activeNavTarget = sectionId === "booking" ? "rooms" : sectionId;
  document.querySelectorAll("[data-dashboard-target]").forEach((item) => {
    item.classList.toggle(
      "active",
      item.dataset.dashboardTarget === activeNavTarget,
    );
  });

  document.dispatchEvent(
    new CustomEvent("dashboard:section-change", {
      detail: { sectionId },
    }),
  );
}

function setupRoomFilters() {
  const filters = document.getElementById("bookingFilters");
  const searchInput = document.getElementById("roomSearchInput");

  if (filters) {
    filters.addEventListener("submit", function (event) {
      event.preventDefault();
      searchRooms();
    });
  }

  if (searchInput) {
    searchInput.addEventListener("input", applyRoomFilters);
  }
}

function setDefaultDates() {
  const checkInInput = document.getElementById("checkInDate");
  const checkOutInput = document.getElementById("checkOutDate");

  if (!checkInInput || !checkOutInput) {
    return;
  }

  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const nextDay = new Date(tomorrow);
  nextDay.setDate(nextDay.getDate() + 1);

  checkInInput.value = toInputDate(tomorrow);
  checkOutInput.value = toInputDate(nextDay);
}

function setMinDate() {
  const checkInInput = document.getElementById("checkInDate");
  const checkOutInput = document.getElementById("checkOutDate");

  if (!checkInInput || !checkOutInput) {
    return;
  }

  const today = toInputDate(new Date());
  checkInInput.min = today;
  checkOutInput.min = today;

  checkInInput.addEventListener("change", function () {
    const checkinDate = new Date(this.value);
    const checkoutDate = new Date(checkinDate);
    checkoutDate.setDate(checkoutDate.getDate() + 1);

    checkOutInput.min = toInputDate(checkoutDate);

    if (new Date(checkOutInput.value) <= checkinDate) {
      checkOutInput.value = toInputDate(checkoutDate);
    }
  });
}

function toInputDate(date) {
  return date.toISOString().split("T")[0];
}

async function loadRooms() {
  showLoading("roomsContainer", "Loading rooms...");

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

    currentRooms = await response.json();
  } catch (error) {
    console.error("Error loading rooms:", error);
    currentRooms = [];
  }

  applyRoomFilters();
}

async function loadCurrentUser() {
  const clientName = document.getElementById("clientName");
  const clientAvatar = document.getElementById("clientAvatar");

  if (!clientName) {
    return;
  }

  try {
    const token = localStorage.getItem("jwtToken");
    const headers = {
      "Content-Type": "application/json",
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch("/api/v1/users/me", {
      method: "GET",
      headers,
    });

    if (!response.ok) {
      throw new Error("Failed to load current user");
    }

    const user = await response.json();
    const fullName = [user.firstName, user.lastName].filter(Boolean).join(" ");
    const displayName = fullName || user.email || "Client";

    clientName.textContent = displayName;
    if (clientAvatar) {
      clientAvatar.textContent = getUserInitial(displayName);
      clientAvatar.setAttribute("aria-label", `${displayName} profile`);
    }
  } catch (error) {
    console.error("Error loading current user:", error);
    clientName.textContent = "Client";
    if (clientAvatar) {
      clientAvatar.textContent = "C";
      clientAvatar.setAttribute("aria-label", "Client profile");
    }
  }
}

function getUserInitial(value) {
  return String(value || "Client").trim().charAt(0).toUpperCase() || "C";
}

function searchRooms() {
  const checkIn = document.getElementById("checkInDate").value;
  const checkOut = document.getElementById("checkOutDate").value;

  if (!checkIn || !checkOut) {
    alert("Please select your check-in and check-out dates first");
    return;
  }

  if (new Date(checkIn) >= new Date(checkOut)) {
    alert("The departure date must be after the arrival date");
    return;
  }

  applyRoomFilters();
}

function applyRoomFilters() {
  const adults = Number(document.getElementById("adults")?.value || 1);
  const children = Number(document.getElementById("children")?.value || 0);
  const totalGuests = adults + children;
  const query = (document.getElementById("roomSearchInput")?.value || "")
    .trim()
    .toLowerCase();

  const rooms = currentRooms.filter((room) => {
    const capacityMatches = getRoomCapacity(room) >= totalGuests;
    const searchableText = [
      getRoomDisplayName(room),
      room.description,
      room.type,
      room.roomType,
      room.number,
      room.roomNumber,
    ]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();

    return capacityMatches && (!query || searchableText.includes(query));
  });

  displayRooms(rooms);
}

function displayRooms(rooms) {
  const container = document.getElementById("roomsContainer");

  if (!container) {
    return;
  }

  if (!rooms || rooms.length === 0) {
    container.innerHTML = `
      <div class="empty-row">
        No rooms available for the selected criteria.
      </div>
    `;
    return;
  }

  container.innerHTML = rooms
    .map((room) => {
      const roomName = getRoomDisplayName(room);
      const price = getRoomPrice(room);
      const available = isRoomAvailable(room);
      const status = getRoomStatusText(getRoomStatus(room));

      return `
        <article class="room-card">
          <img
            class="room-thumb"
            src="${escapeAttribute(getRoomImage(room))}"
            alt="${escapeAttribute(roomName)}"
            width="78"
            height="52"
            loading="lazy"
            decoding="async"
          />
          <div class="room-copy">
            <h2 class="room-title">${escapeHtml(roomName)}</h2>
            <p class="room-meta">
              ${getRoomCapacity(room)} guest${getRoomCapacity(room) > 1 ? "s" : ""}
              - ${escapeHtml(status)}
            </p>
          </div>
          <button
            class="room-cta"
            type="button"
            onclick="openReservationPage(${Number(room.id)})"
            ${available ? "" : "disabled"}
          >
            ${formatCurrency(price)} -&gt;
          </button>
        </article>
      `;
    })
    .join("");
}

function getRoomDisplayName(room) {
  if (room.name && room.name.trim()) {
    return room.name.trim();
  }

  const number = room.number || room.roomNumber;
  const type = room.type || room.roomType;
  const typeNames = {
    STANDARD: "Standard Room",
    SUPERIOR: "Superior Room",
    DELUXE: "Deluxe Room",
    JUNIOR_SUITE: "Junior Suite",
    SUITE: "Suite",
    PRESIDENTIAL_SUITE: "Presidential Suite",
    FAMILY_ROOM: "Family Room",
    FAMILY: "Family Room",
    TWIN: "Twin Room",
    DOUBLE: "Double Room",
    SINGLE: "Single Room",
  };

  if (number) {
    return `${number} - ${typeNames[type] || type || "Room"}`;
  }

  return typeNames[type] || type || "Room";
}

function getRoomCapacity(room) {
  return Number(room.capacity || room.maxOccupancy || 1);
}

function getRoomPrice(room) {
  return Number(room.price || room.basePrice || 129);
}

function getRoomStatus(room) {
  return room.status || room.roomStatus || room.availability || "AVAILABLE";
}

function isRoomAvailable(room) {
  const status = getRoomStatus(room).toUpperCase();
  return status === "AVAILABLE" || status === "LIMITED";
}

function getRoomStatusText(status) {
  switch ((status || "").toUpperCase()) {
    case "AVAILABLE":
      return "Available";
    case "LIMITED":
      return "Last rooms";
    case "OCCUPIED":
      return "Occupied";
    case "MAINTENANCE":
      return "Maintenance";
    case "OUT_OF_ORDER":
      return "Out of order";
    case "UNAVAILABLE":
      return "Unavailable";
    default:
      return "Available";
  }
}

function getRoomImage(room) {
  return room.imageUrl || "/image/logo_noBg_no_name.webp";
}

function formatCurrency(amount) {
  return new Intl.NumberFormat("fr-FR", {
    style: "currency",
    currency: "EUR",
    maximumFractionDigits: 0,
  }).format(amount);
}

function showReservationConfirmation() {
  const existingToast = document.querySelector(".toast-message");
  if (existingToast) {
    existingToast.remove();
  }

  document.body.insertAdjacentHTML(
    "afterbegin",
    `
      <div class="toast-message" role="status">
        <p><strong>Reservation confirmed!</strong> You will receive a confirmation email shortly.</p>
        <button type="button" onclick="this.closest('.toast-message').remove()">OK</button>
      </div>
    `,
  );

  window.setTimeout(() => {
    document.querySelector(".toast-message")?.remove();
  }, 5000);
}

function setupLogout() {
  const logoutLink = document.querySelector("[data-logout-link]");

  if (!logoutLink) {
    return;
  }

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
}

function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString("fr-FR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function showLoading(containerId, message) {
  const container = document.getElementById(containerId);

  if (!container) {
    return;
  }

  container.innerHTML = `<div class="loading-row">${escapeHtml(message || "Loading...")}</div>`;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttribute(value) {
  return escapeHtml(value).replaceAll("`", "&#096;");
}
