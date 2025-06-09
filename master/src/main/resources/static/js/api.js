export async function fetchWithAuth(url, options = {}) {
  const token = localStorage.getItem("jwtToken");
  if (!token) {
    alert("No JWT token found, please login.");
    window.location.href = "/clientLogin";
    throw new Error("No JWT token found");
  }

  const headers = {
    ...(options.headers || {}),
    "Authorization": `Bearer ${token}`,
    "Content-Type": "application/json",
  };

  const response = await fetch(url, { ...options, headers });

  if (response.status === 403) {
    alert("Access denied. Please login again.");
    window.location.href = "/clientLogin";
    throw new Error("Forbidden");
  }

  return response;
}
