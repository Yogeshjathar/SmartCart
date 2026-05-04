const API_BASE = import.meta.env.VITE_API_BASE ?? "/api/v1";

async function request(path, { method = "GET", token, body, params } = {}) {
  const url = new URL(`${API_BASE}${path}`, window.location.origin);

  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, value);
      }
    });
  }

  const response = await fetch(url.toString(), {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: body ? JSON.stringify(body) : undefined
  });

  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof payload === "string"
        ? payload
        : payload?.message || payload?.error || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return payload;
}

export const api = {
  register: (body) => request("/users/register", { method: "POST", body }),
  login: (body) => request("/auth/login", { method: "POST", body }),
  getProducts: (token) => request("/products", { token }),
  getInventory: (productId, token) => request(`/inventory/${productId}`, { token }),
  createOrder: (body, token) => request("/orders", { method: "POST", body, token }),
  getOrderHistory: (userId, token) => request(`/orders/user/${userId}`, { token }),
  getOrderWorkflow: (orderId, token) => request(`/orders/${orderId}/workflow`, { token }),
  cancelOrder: (orderId, token) => request(`/orders/${orderId}/cancel`, { method: "PUT", token }),
  getPaymentForOrder: (orderId, token) => request(`/payments/order/${orderId}`, { token }),
  getNotifications: (userId, token) => request(`/notifications/user/${userId}`, { token }),
  createProduct: (body, token) => request("/products", { method: "POST", body, token }),
  addInventory: (body, token) => request("/inventory", { method: "POST", body, token })
};
