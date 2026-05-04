import { createContext, startTransition, useContext, useEffect, useMemo, useState } from "react";
import { clearStoredSession, decodeJwt, readStoredSession, writeStoredSession } from "../lib/session";

const AppContext = createContext(null);

function toSession(authResponse) {
  const token = authResponse.accessToken;
  const claims = decodeJwt(token);
  const roleValue = claims?.roles ?? "";
  const firstName = claims?.firstName ?? "";
  const lastName = claims?.lastName ?? "";
  const fullName = [firstName, lastName].filter(Boolean).join(" ").trim();

  return {
    accessToken: token,
    tokenType: authResponse.tokenType,
    expiresIn: authResponse.expiresIn,
    userId: claims?.sub ?? "",
    displayName: claims?.name ?? fullName,
    role: Array.isArray(roleValue) ? roleValue[0] : roleValue,
    issuedAt: Date.now()
  };
}

export function AppProvider({ children }) {
  const [session, setSession] = useState(() => readStoredSession());
  const [cart, setCart] = useState([]);

  useEffect(() => {
    const savedCart = window.localStorage.getItem("smartcart.cart");
    if (savedCart) {
      try {
        setCart(JSON.parse(savedCart));
      } catch {
        setCart([]);
      }
    }
  }, []);

  useEffect(() => {
    window.localStorage.setItem("smartcart.cart", JSON.stringify(cart));
  }, [cart]);

  const value = useMemo(
    () => ({
      session,
      cart,
      isAuthenticated: Boolean(session?.accessToken),
      isAdmin: (session?.role ?? "").includes("ADMIN"),
      saveSession(authResponse) {
        const nextSession = toSession(authResponse);
        writeStoredSession(nextSession);
        startTransition(() => setSession(nextSession));
      },
      logout() {
        clearStoredSession();
        startTransition(() => {
          setSession(null);
          setCart([]);
        });
        window.localStorage.removeItem("smartcart.cart");
      },
      addToCart(product) {
        setCart((current) => {
          const existing = current.find((item) => item.id === product.id);
          if (existing) {
            return current.map((item) =>
              item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item
            );
          }
          return [
            ...current,
            {
              id: product.id,
              name: product.name,
              price: Number(product.price),
              currency: product.currency,
              quantity: 1
            }
          ];
        });
      },
      updateCartQuantity(productId, quantity) {
        setCart((current) =>
          current
            .map((item) => (item.id === productId ? { ...item, quantity } : item))
            .filter((item) => item.quantity > 0)
        );
      },
      clearCart() {
        setCart([]);
      }
    }),
    [cart, session]
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useAppContext() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useAppContext must be used inside AppProvider");
  }
  return context;
}
