export function decodeJwt(token) {
  if (!token) {
    return null;
  }

  try {
    const [, payload] = token.split(".");
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(normalized);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function readStoredSession() {
  const raw = window.localStorage.getItem("smartcart.session");
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function writeStoredSession(session) {
  window.localStorage.setItem("smartcart.session", JSON.stringify(session));
}

export function clearStoredSession() {
  window.localStorage.removeItem("smartcart.session");
}
