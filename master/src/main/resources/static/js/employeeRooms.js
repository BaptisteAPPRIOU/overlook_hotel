document.addEventListener("DOMContentLoaded", () => {
    const API = "/api/v1/rooms";
    const token = localStorage.getItem("jwtToken");
    if (!token) {
        alert("Please log in first.");
        return window.location.href = "/employeeLogin";
    }

    const headers = {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
    };

    // Point at the <tbody> where rows will go
    const tbody = document.getElementById("roomsTableBody");
    const form  = document.getElementById("roomForm");
    const numIn = document.getElementById("roomNumber");
    const typeS = document.getElementById("roomType");
    const occCb = document.getElementById("isOccupied");

    let editId = null;

    function boolToIcon(val) {
        return val ? "✔️" : "❌";
    }

    async function request(url, opts = {}) {
        const res = await fetch(url, { ...opts, headers });
        if (res.status === 403) {
            alert("Access denied — please log in again.");
            window.location.href = "/employeeLogin";
            return;
        }
        return res;
    }

    async function load() {
        const res   = await request(API);
        const rooms = await res.json();

        // Clear existing rows
        tbody.innerHTML = "";

        rooms.forEach(r => {
            const row = tbody.insertRow();
            row.innerHTML = `
        <td>${r.roomNumber}</td>
        <td>${r.roomType}</td>
        <td>${boolToIcon(r.occupied)}</td>
        <td>
          <button data-id="${r.id}" class="btn-icon edit" title="Edit room">✏️</button>
          <button data-id="${r.id}" class="btn-icon del"  title="Delete room">🗑️</button>
        </td>
      `;
        });

        // Wire up edit/delete handlers on the newly-added buttons
        tbody.querySelectorAll(".edit").forEach(btn =>
            btn.addEventListener("click", () => startEdit(btn.dataset.id))
        );
        tbody.querySelectorAll(".del").forEach(btn =>
            btn.addEventListener("click", () => deleteRoom(btn.dataset.id))
        );
    }

    async function startEdit(id) {
        const res = await request(`${API}/${id}`);
        const r   = await res.json();
        editId       = id;
        numIn.value  = r.roomNumber;
        typeS.value  = r.roomType;
        occCb.checked = r.occupied;
        form.querySelector("button").textContent = "Update Room";
    }

    async function deleteRoom(id) {
        if (!confirm("Are you sure you want to delete this room?")) return;
        await request(`${API}/${id}`, { method: "DELETE" });
        load();
    }

    form.addEventListener("submit", async e => {
        e.preventDefault();

        const payload = {
            roomNumber: numIn.value,
            roomType:   typeS.value,
            occupied:   occCb.checked
        };

        const opts = {
            method: editId ? "PUT" : "POST",
            body:   JSON.stringify(payload)
        };

        const url = editId ? `${API}/${editId}` : API;
        await request(url, opts);

        // Reset form & button text
        editId = null;
        form.reset();
        form.querySelector("button").textContent = "Create Room";

        load();
    });

    // Initial load
    load();
});
