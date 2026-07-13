(function () {
  let roomReviews = [];
  let loaded = false;

  document.addEventListener("DOMContentLoaded", function () {
    const reviewsNavButton = document.querySelector('[data-cat="reviews"]');
    const searchInput = document.getElementById("employeeReviewSearch");
    const ratingFilter = document.getElementById("employeeReviewRatingFilter");

    reviewsNavButton?.addEventListener("click", function () {
      if (!loaded) {
        loadRoomReviews();
      }
    });

    searchInput?.addEventListener("input", renderRoomReviews);
    ratingFilter?.addEventListener("change", renderRoomReviews);
  });

  async function loadRoomReviews() {
    const list = document.getElementById("employeeRoomReviewsList");

    try {
      setReviewsMessage("Loading reviews...");
      const response = await fetch("/api/v1/room-reviews", {
        headers: {
          Authorization: "Bearer " + localStorage.getItem("jwtToken"),
        },
      });

      if (!response.ok) {
        throw new Error("Unable to load room reviews");
      }

      const result = await response.json();
      loaded = true;
      roomReviews = result.reviews || [];
      updateReviewStats(result);
      renderRoomReviews();
    } catch (error) {
      console.error("Error loading room reviews:", error);
      if (list) {
        list.innerHTML =
          '<div class="employee-reviews-empty">Unable to load reviews.</div>';
      }
    }
  }

  function updateReviewStats(result) {
    setText("employeeReviewsAverage", Number(result.averageRating || 0).toFixed(1));
    setText("employeeReviewsTotal", result.totalReviews || 0);
    setText("employeeReviewsVerified", result.verifiedReviews || 0);
  }

  function renderRoomReviews() {
    const list = document.getElementById("employeeRoomReviewsList");
    const query = document.getElementById("employeeReviewSearch")?.value.trim().toLowerCase() || "";
    const rating = document.getElementById("employeeReviewRatingFilter")?.value || "";

    if (!list) return;

    const visibleReviews = roomReviews.filter((review) => {
      const matchesRating = !rating || String(review.rating) === rating;
      const haystack = [
        review.roomNumber,
        review.roomType,
        review.author,
        review.comment,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return matchesRating && (!query || haystack.includes(query));
    });

    setText("employeeReviewsVisibleCount", visibleReviews.length);

    if (visibleReviews.length === 0) {
      list.innerHTML =
        '<div class="employee-reviews-empty">No reviews match this view.</div>';
      return;
    }

    list.innerHTML = visibleReviews.map(renderReviewItem).join("");
  }

  function renderReviewItem(review) {
    return `
      <article class="employee-review-item">
        <div class="employee-review-room">
          <strong>${escapeHtml(formatRoom(review))}</strong>
          <span>${escapeHtml(humanize(review.roomType || "Room"))}</span>
        </div>
        <div class="employee-review-author">
          <strong>${escapeHtml(review.author || "Client")}</strong>
          <div class="employee-review-stars" aria-label="${review.rating || 0} out of 5 stars">
            ${renderStars(review.rating)}
          </div>
          ${review.verified ? '<span class="employee-review-badge">Verified</span>' : ""}
        </div>
        <p class="employee-review-comment">${escapeHtml(review.comment || "No comment provided.")}</p>
        <time class="employee-review-date">${escapeHtml(formatDate(review.reviewDate || review.createdAt))}</time>
      </article>
    `;
  }

  function formatRoom(review) {
    return review.roomNumber ? `Room ${review.roomNumber}` : "Room";
  }

  function renderStars(rating) {
    const value = Number(rating || 0);
    return `${"★".repeat(value)}${"☆".repeat(Math.max(0, 5 - value))}`;
  }

  function formatDate(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return date.toLocaleDateString("fr-FR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  }

  function humanize(value) {
    return String(value)
      .replace(/_/g, " ")
      .toLowerCase()
      .replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  function setReviewsMessage(message) {
    const list = document.getElementById("employeeRoomReviewsList");
    if (list) {
      list.innerHTML = `<div class="employee-reviews-empty">${escapeHtml(message)}</div>`;
    }
  }

  function setText(id, value) {
    const element = document.getElementById(id);
    if (element) {
      element.textContent = value;
    }
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
