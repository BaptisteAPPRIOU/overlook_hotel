document.addEventListener("DOMContentLoaded", () => {
  const API = "/api/v1/rooms"; // Matches the controller @RequestMapping
  const token = localStorage.getItem("jwtToken");
  if (!token) {
    alert("Please log in first.");
    return (window.location.href = "/employeeLogin");
  }

  const headers = {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };

  const tbody = document.getElementById("roomsTableBody");
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

  let editId = null;

  // Displays a simple status marker for occupied rooms.
  function occIcon(status) {
    return status === "OCCUPIED" ? "Yes" : "No";
  }

  async function request(url, opts = {}) {
    const res = await fetch(url, { ...opts, headers });
    if (res.status === 403) {
      alert("Access denied - please log in again.");
      return (window.location.href = "/employeeLogin");
    }
    return res;
  }

  function featureIcons(room) {
    return `
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:4px; font-size:1.2em; text-align:center;">
        <span title="Projector">${room.hasProjector ? "Y" : "-"}</span>
        <span title="Whiteboard">${room.hasWhiteboard ? "Y" : "-"}</span>
        <span title="Video conference">${room.hasVideoConference ? "Y" : "-"}</span>
        <span title="Air conditioning">${room.hasAirConditioning ? "Y" : "-"}</span>
        </div>
    `;
  }

  // Loads and renders the room list.
  async function load() {
    // Clear first to avoid duplicate rows.
    tbody.innerHTML = "";

    const res = await request(API);
    if (!res.ok) {
      console.error("Failed to load rooms:", res.status, await res.text());
      return;
    }
    const rooms = await res.json();
    if (!Array.isArray(rooms)) {
      console.error("Expected rooms array but got:", rooms);
      return;
    }

    rooms.forEach((room) => {
      const row = tbody.insertRow();
      row.innerHTML = `
        <td>${room.number}</td>
        <td>${room.type}</td>
        <td>${room.capacity ?? "-"}</td>
        <td>${featureIcons(room)}</td>
        <td>${room.price?.toFixed(2) ?? "-"} EUR</td>
        <td>${room.status} (${occIcon(room.status)})</td>
        <td>
            <button data-id="${room.id}" class="btn btn-sm btn-outline-primary edit">Edit</button>
            <button data-id="${room.id}" class="btn btn-sm btn-outline-danger del">Delete</button>
        </td>
        `;
    });

    tbody
      .querySelectorAll(".edit")
      .forEach((btn) =>
        btn.addEventListener("click", () => startEdit(btn.dataset.id)),
      );
    tbody
      .querySelectorAll(".del")
      .forEach((btn) =>
        btn.addEventListener("click", () => deleteRoom(btn.dataset.id)),
      );
  }

  // Fills the form for editing.
  async function startEdit(id) {
    const res = await request(`${API}/${id}`);
    const room = await res.json();
    editId = id;
    numInput.value = room.number;
    typeSelect.value = room.type;
    occCb.checked = room.status === "OCCUPIED";
    capInput.value = room.capacity;
    priceInput.value = room.price;
    projCb.checked = room.hasProjector;
    whiteCb.checked = room.hasWhiteboard;
    vidCb.checked = room.hasVideoConference;
    acCb.checked = room.hasAirConditioning;
    form.querySelector("button").textContent = "Update Room";
  }

  // Deletes a room.
  async function deleteRoom(id) {
    if (!confirm("Are you sure you want to delete this room?")) {
      return;
    }
    await request(`${API}/${id}`, { method: "DELETE" });
    load();
  }

  // Creates or updates a room.
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const payload = {
      number: numInput.value,
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
    await request(url, {
      method,
      body: JSON.stringify(payload),
    });

    editId = null;
    form.reset();
    form.querySelector("button").textContent = "Create Room";
    load();
  });

  load();
});
