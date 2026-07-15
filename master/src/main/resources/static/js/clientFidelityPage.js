let fidelityLoaded = false;
let fidelityState = {
  points: 0,
  level: "BRONZE",
};

document.addEventListener("DOMContentLoaded", function () {
  const refreshButton = document.getElementById("refreshFidelityButton");

  if (refreshButton) {
    refreshButton.addEventListener("click", function () {
      loadFidelityPage(true);
    });
  }

  document.addEventListener("dashboard:section-change", function (event) {
    if (event.detail?.sectionId === "fidelity") {
      loadFidelityPage(false);
    }
  });

  if (document.getElementById("fidelity")?.classList.contains("active")) {
    loadFidelityPage(false);
  }
});

async function loadFidelityPage(forceRefresh) {
  if (fidelityLoaded && !forceRefresh) {
    return;
  }

  setFidelityStatus("Loading loyalty data...");
  setFidelityBusy(true);

  try {
    const [summary, rewards] = await Promise.all([
      fetchFidelityJson("/api/v1/fidelity/summary"),
      fetchFidelityJson("/api/v1/fidelity/redemption-options"),
    ]);

    fidelityLoaded = true;
    fidelityState = {
      points: Number(summary.currentPoints || 0),
      level: String(summary.level || "BRONZE"),
    };

    renderFidelitySummary(summary);
    renderFidelityRewards(rewards);
    setFidelityStatus("");
  } catch (error) {
    console.error("Unable to load loyalty data:", error);
    renderFidelityError();
    setFidelityStatus("Unable to load loyalty data.");
  } finally {
    setFidelityBusy(false);
  }
}

async function fetchFidelityJson(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...fidelityHeaders(),
      ...(options.headers || {}),
    },
  });

  if (response.status === 401) {
    setFidelityStatus("Session expired. Redirecting to login...");
    window.location.href = "/clientLogin";
    throw new Error("Unauthorized");
  }

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }

  return response.json();
}

function fidelityHeaders() {
  const token = localStorage.getItem("jwtToken");
  const headers = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return headers;
}

function renderFidelitySummary(summary) {
  const points = Number(summary.currentPoints || 0);
  const level = String(summary.level || "BRONZE");
  const pointsToNext = Number(summary.pointsToNextLevel || 0);
  const discount = Number(summary.discountPercentage || 0);
  const progress = getFidelityProgress(points, level);

  setText("fidelityCurrentPoints", formatPoints(points));
  setText("fidelityLevelName", `${formatLevel(level)} member`);
  setText("fidelityNextLevel", getNextLevelLabel(level));
  setText(
    "fidelityProgressText",
    pointsToNext > 0
      ? `${formatPoints(pointsToNext)} needed for ${getNextLevelLabel(level)}`
      : "Maximum level reached",
  );
  setText("fidelityDiscount", formatDiscount(discount));

  const track = document.getElementById("fidelityProgressTrack");
  const bar = document.getElementById("fidelityProgressBar");

  if (track && bar) {
    track.setAttribute("aria-valuenow", String(progress));
    bar.style.width = `${progress}%`;
  }
}

function renderFidelityRewards(rewards) {
  const list = document.getElementById("fidelityRewardsList");
  const count = document.getElementById("fidelityRewardsCount");

  if (!list) {
    return;
  }

  if (!Array.isArray(rewards) || rewards.length === 0) {
    list.innerHTML = '<div class="fidelity-empty">No rewards available.</div>';
    if (count) {
      count.textContent = "0 rewards";
    }
    return;
  }

  if (count) {
    const availableCount = rewards.filter((reward) => reward.available).length;
    count.textContent = `${availableCount}/${rewards.length} available`;
  }

  list.innerHTML = rewards.map(renderFidelityReward).join("");

  list.querySelectorAll("[data-fidelity-redeem]").forEach((button) => {
    button.addEventListener("click", function () {
      redeemFidelityReward(Number(this.dataset.pointsCost));
    });
  });
}

function renderFidelityReward(reward) {
  const available = Boolean(reward.available);
  const pointsCost = Number(reward.pointsCost || 0);
  const title = escapeFidelityHtml(reward.title || "Reward");
  const description = escapeFidelityHtml(reward.description || "");

  return `
    <article class="fidelity-reward ${available ? "" : "locked"}">
      <div>
        <h3>${title}</h3>
        <p>${description}</p>
        <span class="fidelity-reward-cost">${formatPoints(pointsCost)}</span>
      </div>
      <button
        class="fidelity-reward-button"
        type="button"
        data-fidelity-redeem
        data-points-cost="${pointsCost}"
        ${available ? "" : "disabled"}>
        Redeem
      </button>
    </article>
  `;
}

async function redeemFidelityReward(pointsCost) {
  if (!Number.isFinite(pointsCost) || pointsCost <= 0) {
    return;
  }

  setFidelityStatus("Redeeming reward...");
  setFidelityBusy(true);

  try {
    const result = await fetchFidelityJson("/api/v1/fidelity/redeem", {
      method: "POST",
      body: JSON.stringify({ points: pointsCost }),
    });

    fidelityLoaded = false;
    setFidelityStatus(result.message || "Reward redeemed.");
    await loadFidelityPage(true);
  } catch (error) {
    console.error("Unable to redeem reward:", error);
    setFidelityStatus("Unable to redeem this reward.");
  } finally {
    setFidelityBusy(false);
  }
}

function renderFidelityError() {
  setText("fidelityCurrentPoints", "-");
  setText("fidelityLevelName", "Unavailable");
  setText("fidelityNextLevel", "-");
  setText("fidelityProgressText", "Progress unavailable");
  setText("fidelityDiscount", "-");

  const list = document.getElementById("fidelityRewardsList");
  if (list) {
    list.innerHTML =
      '<div class="fidelity-empty">Rewards cannot be loaded right now.</div>';
  }
}

function getFidelityProgress(points, level) {
  const thresholds = {
    BRONZE: [0, 200],
    SILVER: [200, 500],
    GOLD: [500, 1000],
    DIAMOND: [1000, 1000],
  };
  const range = thresholds[level] || thresholds.BRONZE;

  if (level === "DIAMOND") {
    return 100;
  }

  const progress = ((points - range[0]) / (range[1] - range[0])) * 100;
  return Math.max(0, Math.min(100, Math.round(progress)));
}

function getNextLevelLabel(level) {
  const nextLevels = {
    BRONZE: "Silver",
    SILVER: "Gold",
    GOLD: "Diamond",
    DIAMOND: "Diamond",
  };
  return nextLevels[level] || "Silver";
}

function formatLevel(level) {
  return String(level || "BRONZE")
    .toLowerCase()
    .replace(/^\w/, (letter) => letter.toUpperCase());
}

function formatPoints(points) {
  return `${new Intl.NumberFormat("fr-FR").format(Number(points || 0))} points`;
}

function formatDiscount(discount) {
  const percent = discount > 1 ? discount : discount * 100;
  return `${Math.round(percent)}%`;
}

function setText(id, value) {
  const element = document.getElementById(id);
  if (element) {
    element.textContent = value;
  }
}

function setFidelityStatus(message) {
  setText("fidelityStatus", message || "");
}

function setFidelityBusy(isBusy) {
  const refreshButton = document.getElementById("refreshFidelityButton");
  if (refreshButton) {
    refreshButton.disabled = isBusy;
  }
}

function escapeFidelityHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
