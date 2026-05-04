import { useState } from "react";
import { api } from "../lib/api";
import { useAppContext } from "../context/AppContext";

const registerDefaults = {
  firstName: "",
  lastName: "",
  phoneNo: "",
  email: "",
  password: "",
  role: "ROLE_USER"
};

const loginDefaults = {
  email: "",
  password: ""
};

export function AuthPage() {
  const { saveSession } = useAppContext();
  const [mode, setMode] = useState("login");
  const [loginForm, setLoginForm] = useState(loginDefaults);
  const [registerForm, setRegisterForm] = useState(registerDefaults);
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function handleLogin(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setStatus("");

    try {
      const auth = await api.login(loginForm);
      saveSession(auth);
      setStatus("Logged in successfully.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    setStatus("");

    try {
      await api.register(registerForm);
      setStatus("Account created. You can log in now.");
      setMode("login");
      setLoginForm({ email: registerForm.email, password: registerForm.password });
      setRegisterForm(registerDefaults);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-shell">
      <section className="auth-copy">
        <p className="eyebrow">SmartCart</p>
        <h1>Customer storefront and operations console</h1>
        <p>
          Use one app for browsing products, placing orders, reviewing workflow state, checking
          payment progress, and handling admin inventory tasks.
        </p>
      </section>

      <section className="auth-card">
        <div className="auth-switch">
          <button className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>
            Login
          </button>
          <button className={mode === "register" ? "active" : ""} onClick={() => setMode("register")}>
            Register
          </button>
        </div>

        {mode === "login" ? (
          <form className="stacked-form" onSubmit={handleLogin}>
            <label>
              Email
              <input
                value={loginForm.email}
                onChange={(event) => setLoginForm({ ...loginForm, email: event.target.value })}
                type="email"
                required
              />
            </label>
            <label>
              Password
              <input
                value={loginForm.password}
                onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })}
                type="password"
                required
              />
            </label>
            <button type="submit" disabled={busy}>
              {busy ? "Signing in..." : "Sign In"}
            </button>
          </form>
        ) : (
          <form className="stacked-form" onSubmit={handleRegister}>
            <label>
              First name
              <input
                value={registerForm.firstName}
                onChange={(event) => setRegisterForm({ ...registerForm, firstName: event.target.value })}
                required
              />
            </label>
            <label>
              Last name
              <input
                value={registerForm.lastName}
                onChange={(event) => setRegisterForm({ ...registerForm, lastName: event.target.value })}
                required
              />
            </label>
            <label>
              Phone
              <input
                value={registerForm.phoneNo}
                onChange={(event) => setRegisterForm({ ...registerForm, phoneNo: event.target.value })}
                required
              />
            </label>
            <label>
              Email
              <input
                value={registerForm.email}
                onChange={(event) => setRegisterForm({ ...registerForm, email: event.target.value })}
                type="email"
                required
              />
            </label>
            <label>
              Password
              <input
                value={registerForm.password}
                onChange={(event) => setRegisterForm({ ...registerForm, password: event.target.value })}
                type="password"
                required
              />
            </label>
            <label>
              Role
              <select
                value={registerForm.role}
                onChange={(event) => setRegisterForm({ ...registerForm, role: event.target.value })}
              >
                <option value="ROLE_USER">Customer</option>
                <option value="ROLE_ADMIN">Admin</option>
              </select>
            </label>
            <button type="submit" disabled={busy}>
              {busy ? "Creating account..." : "Create Account"}
            </button>
          </form>
        )}

        {status ? <p className="success-text">{status}</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
      </section>
    </div>
  );
}
