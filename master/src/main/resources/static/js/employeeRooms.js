document.addEventListener("DOMContentLoaded", () => {
    const API = "/api/v1/rooms";              // Singulier, comme dans votre @RequestMapping
    const token = localStorage.getItem("jwtToken");
    if (!token) {
        alert("Please log in first.");
        return window.location.href = "/employeeLogin";
    }

    const headers = {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
    };

    const tbody = document.getElementById("roomsTableBody");
    const form = document.getElementById("roomForm");
    const numInput = document.getElementById("roomNumber");
    const typeSelect = document.getElementById("roomType");
    const occCb = document.getElementById("isOccupied");
    const capInput     = document.getElementById("roomCapacity");
    const priceInput   = document.getElementById("roomPrice");
    const projCb       = document.getElementById("hasProjector");
    const whiteCb      = document.getElementById("hasWhiteboard");
    const vidCb        = document.getElementById("hasVideoConference");
    const acCb         = document.getElementById("hasAirConditioning");

    let editId = null;

    // Affiche ✔️ si status === "OCCUPIED", ❌ sinon
    function occIcon(status) {
        return status === "OCCUPIED" ? "✔️" : "❌";
    }

    async function request(url, opts = {}) {
        const res = await fetch(url, { ...opts, headers });
        if (res.status === 403) {
            alert("Access denied — please log in again.");
            return window.location.href = "/employeeLogin";
        }
        return res;
    }

    function featureIcons(room) {
    return `
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:4px; font-size:1.2em; text-align:center;">
        <span title="Projector">${room.hasProjector ? '📽️' : '—'}</span>
        <span title="Whiteboard">${room.hasWhiteboard ? '🖋️' : '—'}</span>
        <span title="Video Conf.">${room.hasVideoConference ? '🎥' : '—'}</span>
        <span title="Air Conditioning">${room.hasAirConditioning ? '❄️' : '—'}</span>
        </div>
    `;
    }

    // Charge et affiche la liste des rooms
    async function load() {
    // on vide d'abord pour éviter affichage doublon
    tbody.innerHTML = '';

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

    rooms.forEach(r => {
        const row = tbody.insertRow();
        row.innerHTML = `
        <td>${r.number}</td>
        <td>${r.type}</td>
        <td>${r.capacity ?? '–'}</td>
        <td>${featureIcons(r)}</td>
        <td>${r.price?.toFixed(2) ?? '–'} €</td>
        <td>${r.status}</td>
        <td>
            <button data-id="${r.id}" class="btn btn-sm btn-outline-primary edit">✏️</button>
            <button data-id="${r.id}" class="btn btn-sm btn-outline-danger del">🗑️</button>
        </td>
        `;
    });

    // handlers
    tbody.querySelectorAll(".edit").forEach(btn =>
        btn.addEventListener("click", () => startEdit(btn.dataset.id))
    );
    tbody.querySelectorAll(".del").forEach(btn =>
        btn.addEventListener("click", () => deleteRoom(btn.dataset.id))
    );
    }

    // Remplit le form pour édition
    async function startEdit(id) {
        const res = await request(`${API}/${id}`);
        const r = await res.json();
        editId = id;
        numInput.value = r.number;
        typeSelect.value = r.type;
        occCb.checked = (r.status === "OCCUPIED");
        capInput.value   = r.capacity;
        priceInput.value = r.price;
        projCb.checked   = r.hasProjector;
        whiteCb.checked  = r.hasWhiteboard;
        vidCb.checked    = r.hasVideoConference;
        acCb.checked     = r.hasAirConditioning;
        form.querySelector("button").textContent = "Update Room";

    }

    // Supprime une room
    async function deleteRoom(id) {
        if (!confirm("Are you sure you want to delete this room?")) return;
        await request(`${API}/${id}`, { method: "DELETE" });
        load();
    }

    // Création / mise à jour
    form.addEventListener("submit", async e => {
        e.preventDefault();
        const payload = {
            number: numInput.value,
            type:   typeSelect.value,
            capacity:           parseInt(capInput.value, 10),
            price:              parseFloat(priceInput.value),
            has_projector:      projCb.checked,
            has_whiteboard:     whiteCb.checked,
            has_video_conference: vidCb.checked,
            has_air_conditionning: acCb.checked,
            status:             occCb.checked ? "OCCUPIED" : "AVAILABLE"
        };
        const method = editId ? "PUT" : "POST";
        const url    = editId ? `${API}/${editId}` : API;
        await request(url, {
            method,
            body: JSON.stringify(payload)
        });
        // reset form
        editId = null;
        form.reset();
        form.querySelector("button").textContent = "Create Room";

        load();
    });

    // Initial load
    load();
});
