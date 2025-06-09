/**
 * Performs a fetch request with JWT authentication.
 *
 * Retrieves the JWT token from localStorage and adds it to the Authorization header.
 * If the token is missing, alerts the user and redirects to the login page.
 * If the response status is 403 (Forbidden), alerts the user and redirects to the login page.
 *
 * @async
 * @param {string} url - The URL to fetch.
 * @param {Object} [options={}] - Optional fetch options.
 * @param {Object} [options.headers] - Additional headers to include in the request.
 * @returns {Promise<Response>} The fetch response object.
 * @throws {Error} If no JWT token is found or if access is forbidden.
 */

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
