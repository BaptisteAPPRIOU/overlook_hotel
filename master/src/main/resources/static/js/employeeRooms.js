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

    const table = document.getElementById("roomsTable");
    const form  = document.getElementById("roomForm");
    const numIn = document.getElementById("roomNumber");
    const typeS = document.getElementById("roomType");
    const occCb = document.getElementById("isOccupied");
    let editId = null;

    async function request(url, opts = {}) {
        const res = await fetch(url, { ...opts, headers });
        if (res.status === 403) {
            alert("Access denied — please log in again.");
            return window.location.href = "/employeeLogin";
        }
        return res;
    }

    async function load() {
        const res = await request(API);
        const rooms = await res.json();
        table.innerHTML = `
      <tr><th>ID</th><th>Number</th><th>Type</th><th>Occupied</th><th>Actions</th></tr>
    `;
        rooms.forEach(r => {
            const row = table.insertRow();
            row.innerHTML = `
        <td>${r.id}</td>
        <td>${r.roomNumber}</td>
        <td>${r.roomType}</td>
        <td>${r.occupied}</td>
        <td>
          <button data-id="${r.id}" class="edit">✏️</button>
          <button data-id="${r.id}" class="del">🗑️</button>
        </td>`;
        });
        table.querySelectorAll(".edit").forEach(btn =>
            btn.addEventListener("click", () => startEdit(btn.dataset.id))
        );
        table.querySelectorAll(".del").forEach(btn =>
            btn.addEventListener("click", () => deleteRoom(btn.dataset.id))
        );
    }

    async function startEdit(id) {
        const r = await (await request(`${API}/${id}`)).json();
        editId = id;
        numIn.value = r.roomNumber;
        typeS.value = r.roomType;
        occCb.checked = r.occupied;
        form.querySelector("button").textContent = "Update Room";
    }

    async function deleteRoom(id) {
        if (!confirm("Sure you want to delete?")) return;
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
            body: JSON.stringify(payload)
        };
        const url = editId ? `${API}/${editId}` : API;
        await request(url, opts);
        editId = null;
        form.reset();
        form.querySelector("button").textContent = "Create Room";
        load();
    });

    load();
});
