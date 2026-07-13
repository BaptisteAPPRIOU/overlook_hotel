(function () {
  const DAYS = [
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
    "Sunday",
  ];

  let currentWeekStart = getWeekStart(new Date());
  let currentWeekSchedule = {};
  let initialized = false;

  document.addEventListener("DOMContentLoaded", function () {
    const previousButton = document.getElementById("employeeSchedulePreviousWeek");
    const nextButton = document.getElementById("employeeScheduleNextWeek");
    const scheduleNavButton = document.querySelector('[data-cat="schedule"]');
    const saveButton = document.getElementById("employeeScheduleSave");

    previousButton?.addEventListener("click", function () {
      currentWeekStart = addDays(currentWeekStart, -7);
      loadEmployeeSchedule();
    });

    nextButton?.addEventListener("click", function () {
      currentWeekStart = addDays(currentWeekStart, 7);
      loadEmployeeSchedule();
    });

    scheduleNavButton?.addEventListener("click", initializeEmployeeSchedule);
    saveButton?.addEventListener("click", saveEmployeeScheduleDraft);
  });

  async function initializeEmployeeSchedule() {
    if (initialized) {
      await loadEmployeeSchedule();
      return;
    }

    initialized = true;
    await loadEmployeeSchedule();
  }

  async function loadEmployeeSchedule() {
    const list = document.getElementById("employeeScheduleList");

    try {
      setScheduleMessage("Loading schedule...");

      const scheduleResponse = await fetchWithEmployeeAuth(
        `/api/planning/me/week?start=${toIsoDate(currentWeekStart)}`,
      );

      if (!scheduleResponse.ok) {
        throw new Error("Unable to load schedule");
      }

      const result = await scheduleResponse.json();
      currentWeekSchedule = result.schedule || {};
      renderSchedule();
    } catch (error) {
      console.error("Error loading employee schedule:", error);
      if (list) {
        list.innerHTML =
          '<div class="employee-schedule-empty">No schedule available.</div>';
      }
    }
  }

  async function fetchWithEmployeeAuth(url, options = {}) {
    const token = localStorage.getItem("jwtToken");
    const headers = {
      ...(options.headers || {}),
      Authorization: "Bearer " + token,
    };

    return fetch(url, { ...options, headers });
  }

  function renderSchedule() {
    const list = document.getElementById("employeeScheduleList");
    const label = document.getElementById("employeeScheduleWeekLabel");

    if (!list || !label) return;

    label.textContent = formatShortDate(currentWeekStart);

    if (!currentWeekSchedule || typeof currentWeekSchedule !== "object") {
      setScheduleMessage("No schedule available.");
      return;
    }

    list.innerHTML = DAYS.map((dayName, index) => {
      const date = addDays(currentWeekStart, index);
      const isoDate = toIsoDate(date);
      const shifts = currentWeekSchedule[isoDate] || [];
      const savedDraft = getSavedDraftForDate(isoDate);
      const scheduleValues = getScheduleValues(shifts, savedDraft);

      return `
        <article class="employee-schedule-row">
          <div class="employee-schedule-day">
            ${dayName}
            <span>${formatShortDate(date)}</span>
          </div>
          <div class="employee-schedule-field">
            <span>Time</span>
            <input
              class="employee-schedule-value"
              type="text"
              data-schedule-field="time"
              data-schedule-date="${isoDate}"
              value="${escapeHtml(scheduleValues.time)}"
              placeholder="7h"
            />
          </div>
          <div class="employee-schedule-field">
            <span>Tasks</span>
            <input
              class="employee-schedule-value"
              type="text"
              data-schedule-field="task"
              data-schedule-date="${isoDate}"
              value="${escapeHtml(scheduleValues.task)}"
              placeholder="Rooms cleaning"
            />
          </div>
          <div class="employee-schedule-field">
            <span>Details</span>
            <input
              class="employee-schedule-value"
              type="text"
              data-schedule-field="details"
              data-schedule-date="${isoDate}"
              value="${escapeHtml(scheduleValues.details)}"
              placeholder="Clean rooms A1, B1"
            />
          </div>
        </article>
      `;
    }).join("");
  }

  function getScheduleValues(shifts, savedDraft) {
    if (savedDraft) {
      return savedDraft;
    }

    if (shifts.length > 0) {
      return {
        time: formatShiftHours(shifts),
        task: formatShiftTasks(shifts),
        details: formatShiftDetails(shifts),
      };
    }

    return {
      time: "",
      task: "",
      details: "",
    };
  }

  function saveEmployeeScheduleDraft() {
    const entries = {};
    document.querySelectorAll("[data-schedule-date]").forEach((input) => {
      const date = input.dataset.scheduleDate;
      const field = input.dataset.scheduleField;

      entries[date] = entries[date] || {};
      entries[date][field] = input.value.trim();
    });

    localStorage.setItem(getDraftStorageKey(), JSON.stringify(entries));
    showScheduleFeedback("Schedule saved.");
  }

  function getSavedDraftForDate(date) {
    try {
      const rawDraft = localStorage.getItem(getDraftStorageKey());
      if (!rawDraft) return null;

      const draft = JSON.parse(rawDraft);
      return draft[date] || null;
    } catch (error) {
      console.warn("Unable to read saved schedule draft", error);
      return null;
    }
  }

  function getDraftStorageKey() {
    return `employeeSchedule:${toIsoDate(currentWeekStart)}`;
  }

  function showScheduleFeedback(message) {
    if (typeof window.showNotification === "function") {
      window.showNotification(message, "success");
      return;
    }

    const saveButton = document.getElementById("employeeScheduleSave");
    if (!saveButton) return;

    const originalText = saveButton.textContent;
    saveButton.textContent = message;
    window.setTimeout(() => {
      saveButton.textContent = originalText;
    }, 1400);
  }

  function setScheduleMessage(message) {
    const list = document.getElementById("employeeScheduleList");
    if (list) {
      list.innerHTML = `<div class="employee-schedule-empty">${escapeHtml(message)}</div>`;
    }
  }

  function formatShiftHours(shifts) {
    const total = shifts.reduce((sum, shift) => {
      if (!shift.startTime || !shift.endTime) return sum;
      return sum + calculateHours(shift.startTime, shift.endTime);
    }, 0);

    return total > 0 ? `${trimNumber(total)}h` : "-";
  }

  function formatShiftTasks(shifts) {
    return shifts.map((shift) => humanize(shift.position || shift.type || "Shift")).join(", ");
  }

  function formatShiftDetails(shifts) {
    return shifts
      .map((shift) => {
        if (shift.type === "ABSENCE") {
          return humanize(shift.position || "Absence");
        }

        const start = trimTime(shift.startTime);
        const end = trimTime(shift.endTime);

        return start && end ? `${start} - ${end}` : humanize(shift.status || "Planned");
      })
      .join(", ");
  }

  function calculateHours(startTime, endTime) {
    const [startHour, startMinute] = trimTime(startTime).split(":").map(Number);
    const [endHour, endMinute] = trimTime(endTime).split(":").map(Number);
    return Math.max(0, endHour + endMinute / 60 - (startHour + startMinute / 60));
  }

  function humanize(value) {
    return String(value)
      .replace(/_/g, " ")
      .toLowerCase()
      .replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  function trimTime(value) {
    return value ? String(value).slice(0, 5) : "";
  }

  function trimNumber(value) {
    const number = Number(value);
    return Number.isInteger(number) ? String(number) : number.toFixed(1);
  }

  function getWeekStart(date) {
    const result = new Date(date);
    result.setHours(0, 0, 0, 0);
    const day = result.getDay() || 7;
    result.setDate(result.getDate() - day + 1);
    return result;
  }

  function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
  }

  function formatShortDate(date) {
    return `${String(date.getDate()).padStart(2, "0")}/${String(
      date.getMonth() + 1,
    ).padStart(2, "0")}`;
  }

  function toIsoDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(
      2,
      "0",
    )}-${String(date.getDate()).padStart(2, "0")}`;
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
})();
