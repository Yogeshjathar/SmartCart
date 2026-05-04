import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AppProvider, useAppContext } from "./context/AppContext";
import { AuthPage } from "./pages/AuthPage";
import { ShopPage } from "./pages/ShopPage";

function AppRoutes() {
  const { isAuthenticated } = useAppContext();

  return (
    <Routes>
      <Route path="/" element={isAuthenticated ? <Navigate to="/app" replace /> : <AuthPage />} />
      <Route path="/app" element={isAuthenticated ? <ShopPage /> : <Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AppProvider>
  );
}
