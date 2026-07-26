document.addEventListener("DOMContentLoaded", () => {
  const registerForm = document.getElementById("registerForm");
  const emailPrefix = document.getElementById("emailPrefix");
  const emailHidden = document.getElementById("email");
  const editSelect = document.getElementById("employeeManagementEditId");
  const newFirstName = document.getElementById("newFirstName");
  const newLastName = document.getElementById("newLastName");
  const deleteSelect = document.getElementById("employeeManagementDeleteId");
  const deleteTrigger = document.getElementById("employeeDeleteTrigger");
  const deleteModal = document.getElementById("employeeDeleteModal");
  const deleteModalText = document.getElementById("employeeDeleteModalText");

  function syncEmail() {
    if (!emailPrefix || !emailHidden) {
      return;
    }

    const prefix = emailPrefix.value.trim().replace(/@olh\.fr$/i, "");
    emailHidden.value = prefix ? `${prefix}@olh.fr` : "";
  }

  emailPrefix?.addEventListener("input", syncEmail);
  registerForm?.addEventListener("submit", syncEmail);

  editSelect?.addEventListener("change", () => {
    const option = editSelect.selectedOptions[0];
    if (!option || !option.value) {
      if (newFirstName) newFirstName.value = "";
      if (newLastName) newLastName.value = "";
      return;
    }

    if (newFirstName) {
      newFirstName.value = option.dataset.firstName || "";
    }
    if (newLastName) {
      newLastName.value = option.dataset.lastName || "";
    }
  });

  function closeDeleteModal() {
    deleteModal?.classList.remove("is-open");
    deleteModal?.setAttribute("aria-hidden", "true");
  }

  function openDeleteModal() {
    if (!deleteSelect?.value || !deleteModal) {
      deleteSelect?.focus();
      return;
    }

    const selectedName =
      deleteSelect.selectedOptions[0]?.textContent?.trim() || "this employee";
    if (deleteModalText) {
      deleteModalText.textContent = `Delete ${selectedName}?`;
    }
    deleteModal.classList.add("is-open");
    deleteModal.setAttribute("aria-hidden", "false");
  }

  deleteTrigger?.addEventListener("click", openDeleteModal);

  deleteModal?.addEventListener("click", (event) => {
    if (
      event.target === deleteModal ||
      event.target.closest("[data-employee-delete-cancel]")
    ) {
      closeDeleteModal();
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeDeleteModal();
    }
  });
});
