document.addEventListener("DOMContentLoaded", () => {
  const API = "/api/v1/rooms";
  const token = localStorage.getItem("jwtToken");

  if (!token) {
    alert("Please log in first.");
    window.location.href = "/employeeLogin";
    return;
  }

  const headers = {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };

  const list = document.getElementById("roomsTableBody");
  const form = document.getElementById("roomForm");
  const numInput = document.getElementById("roomNumber");
  const typeSelect = document.getElementById("roomType");
  const occCb = document.getElementById("isOccupied");
  const capInput = document.getElementById("roomCapacity");
  const priceInput = document.getElementById("roomPrice");
  const projCb = document.getElementById("hasProjector");
  const whiteCb = document.getElementById("hasWhiteboard");
  const vidCb = document.getElementById("hasVideoConference");
  const acCb = document.getElementById("hasAirConditioning");
  const submitButton = form?.querySelector(".employee-room-submit");
  const deleteModal = document.getElementById("employeeRoomDeleteModal");
  const deleteTitle = document.getElementById("employeeRoomDeleteTitle");
  const confirmDeleteButton = document.getElementById("confirmEmployeeRoomDelete");

  let editId = null;
  let pendingDeleteId = null;
  let pendingDeleteLabel = "this room";

  async function request(url, opts = {}) {
    const res = await fetch(url, { ...opts, headers });

    if (res.status === 401 || res.status === 403) {
      alert("Access denied - please log in again.");
      window.location.href = "/employeeLogin";
    }

    return res;
  }

  async function loadRooms() {
    if (!list) {
      return;
    }

    list.innerHTML = '<div class="employee-rooms-empty">Loading rooms...</div>';

    const res = await request(API);
    if (!res.ok) {
      console.error("Failed to load rooms:", res.status, await res.text());
      list.innerHTML =
        '<div class="employee-rooms-empty">Unable to load rooms.</div>';
      return;
    }

    const rooms = await res.json();

    if (!Array.isArray(rooms) || rooms.length === 0) {
      list.innerHTML = '<div class="employee-rooms-empty">No rooms found.</div>';
      return;
    }

    list.innerHTML = rooms.map(createRoomRow).join("");
  }

  function createRoomRow(room) {
    const number = room.number || room.roomNumber || "-";
    const type = room.type || room.roomType || "-";
    const status = room.status || room.roomStatus || "AVAILABLE";
    const price = Number(room.price || room.basePrice || 0);

    return `
      <div class="employee-room-row" role="row">
        <span class="employee-room-number" role="cell">${escapeHtml(number)}</span>
        <span class="employee-room-type" role="cell">${escapeHtml(type)}</span>
        <span class="employee-room-capacity" role="cell">${escapeHtml(room.capacity ?? "-")}</span>
        <span class="employee-room-features" role="cell">
          ${featureIcon("air", Boolean(room.hasAirConditioning), getSnowflakeIcon(), "A/C")}
          ${featureIcon("whiteboard", Boolean(room.hasWhiteboard), getPenToolIcon(), "Whiteboard")}
          ${featureIcon("video", Boolean(room.hasVideoConference), getVideoIcon(), "Video")}
          ${featureIcon("projector", Boolean(room.hasProjector), getProjectorIcon(), "Projector")}
        </span>
        <span class="employee-room-price" role="cell">${price ? `${price.toFixed(2)}EUR` : "- EUR"}</span>
        <span class="employee-room-status" role="cell">${escapeHtml(status)}</span>
        <span class="employee-room-actions" role="cell">
          <button class="employee-room-action del" type="button" data-id="${Number(room.id)}" aria-label="Delete room">
            ${getTrashIcon()}
          </button>
          <button class="employee-room-action edit" type="button" data-id="${Number(room.id)}" aria-label="Edit room">
            ${getPencilIcon()}
          </button>
        </span>
      </div>
    `;
  }

  function featureIcon(key, active, icon, label) {
    return `
      <span
        class="employee-room-feature ${active ? "active" : ""}"
        data-feature="${escapeAttribute(key)}"
        title="${escapeAttribute(label)}"
        aria-label="${escapeAttribute(label)}">
        ${icon}
      </span>
    `;
  }

  list?.addEventListener("click", function (event) {
    const editButton = event.target.closest(".edit");
    const deleteButton = event.target.closest(".del");

    if (editButton) {
      startEdit(editButton.dataset.id);
    }

    if (deleteButton) {
      openDeleteModal(deleteButton.dataset.id);
    }
  });

  async function startEdit(id) {
    const res = await request(`${API}/${id}`);

    if (!res.ok) {
      return;
    }

    const room = await res.json();
    editId = id;
    numInput.value = room.number || room.roomNumber || "";
    typeSelect.value = room.type || room.roomType || "";
    occCb.checked = (room.status || room.roomStatus) === "OCCUPIED";
    capInput.value = room.capacity || "";
    priceInput.value = room.price || room.basePrice || "";
    projCb.checked = Boolean(room.hasProjector);
    whiteCb.checked = Boolean(room.hasWhiteboard);
    vidCb.checked = Boolean(room.hasVideoConference);
    acCb.checked = Boolean(room.hasAirConditioning);

    if (submitButton) {
      submitButton.textContent = "Update";
    }
  }

  function openDeleteModal(id) {
    const deleteButton = [...document.querySelectorAll(".employee-room-action.del")].find(
      (button) => button.dataset.id === String(id),
    );
    const row = deleteButton?.closest(".employee-room-row");
    const roomNumber =
      row?.querySelector(".employee-room-number")?.textContent.trim() || "";

    pendingDeleteId = id;
    pendingDeleteLabel = roomNumber ? `room ${roomNumber}` : "this room";

    if (deleteTitle) {
      deleteTitle.textContent = `Delete ${pendingDeleteLabel}?`;
    }

    deleteModal?.classList.add("active");
    deleteModal?.setAttribute("aria-hidden", "false");
    document.body.classList.add("modal-open");
  }

  function closeDeleteModal() {
    pendingDeleteId = null;
    pendingDeleteLabel = "this room";
    deleteModal?.classList.remove("active");
    deleteModal?.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");
  }

  async function deleteRoom(id) {
    if (!id) {
      return;
    }

    const res = await request(`${API}/${id}`, { method: "DELETE" });

    if (!res.ok) {
      alert("Unable to delete this room.");
      return;
    }

    loadRooms();
  }

  document.addEventListener("click", function (event) {
    if (event.target.closest("[data-room-delete-close]")) {
      closeDeleteModal();
    }
  });

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
      closeDeleteModal();
    }
  });

  confirmDeleteButton?.addEventListener("click", async function () {
    const id = pendingDeleteId;
    closeDeleteModal();
    await deleteRoom(id);
  });

  form?.addEventListener("submit", async (event) => {
    event.preventDefault();

    const payload = {
      number: numInput.value.trim(),
      type: typeSelect.value,
      capacity: parseInt(capInput.value, 10),
      price: parseFloat(priceInput.value),
      has_projector: projCb.checked,
      has_whiteboard: whiteCb.checked,
      has_video_conference: vidCb.checked,
      has_air_conditionning: acCb.checked,
      status: occCb.checked ? "OCCUPIED" : "AVAILABLE",
    };
    const method = editId ? "PUT" : "POST";
    const url = editId ? `${API}/${editId}` : API;
    const res = await request(url, {
      method,
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      alert("Unable to save this room.");
      return;
    }

    editId = null;
    form.reset();
    if (submitButton) {
      submitButton.textContent = "Create";
    }
    loadRooms();
  });

  loadRooms();
});

function getSnowflakeIcon() {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="m10 20-1.25-2.5L6 18"></path><path d="M10 4 8.75 6.5 6 6"></path><path d="m14 20 1.25-2.5L18 18"></path><path d="m14 4 1.25 2.5L18 6"></path><path d="m17 21-3-6h-4"></path><path d="m17 3-3 6 1.5 3"></path><path d="M2 12h6.5L10 9"></path><path d="m20 10-1.5 2 1.5 2"></path><path d="M22 12h-6.5L14 15"></path><path d="m4 10 1.5 2L4 14"></path><path d="m7 21 3-6-1.5-3"></path><path d="m7 3 3 6h4"></path>
    </svg>
  `;
}

function getPenToolIcon() {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M15.707 21.293a1 1 0 0 1-1.414 0l-1.586-1.586a1 1 0 0 1 0-1.414l5.586-5.586a1 1 0 0 1 1.414 0l1.586 1.586a1 1 0 0 1 0 1.414z"></path><path d="m18 13-1.375-6.874a1 1 0 0 0-.746-.776L3.235 2.028a1 1 0 0 0-1.207 1.207L5.35 15.879a1 1 0 0 0 .776.746L13 18"></path><path d="m2.3 2.3 7.286 7.286"></path><circle cx="11" cy="11" r="2"></circle>
    </svg>
  `;
}

function getVideoIcon() {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5"></path><rect x="2" y="6" width="14" height="12" rx="2"></rect>
    </svg>
  `;
}

function getProjectorIcon() {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M5 7 3 5"></path><path d="M9 6V3"></path><path d="m13 7 2-2"></path><circle cx="9" cy="13" r="3"></circle><path d="M11.83 12H20a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-4a2 2 0 0 1 2-2h2.17"></path><path d="M16 16h2"></path>
    </svg>
  `;
}

function getTrashIcon() {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"></path><path d="M3 6h18"></path><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
    </svg>
  `;
}

function getPencilIcon() {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M13 21h8"></path><path d="m15 5 4 4"></path><path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"></path>
    </svg>
  `;
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
