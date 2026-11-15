
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";
import { keycloak } from "./auth/keycloak";
import { getCurrentUser } from "./api/userApi";
import "@/i18n";

async function bootstrap() {
  try {
    const authenticated = await keycloak.init({
      onLoad: "login-required",
      pkceMethod: "S256",
      checkLoginIframe: false,
    });

    if (!authenticated) {
      throw new Error("Użytkownik nie jest zalogowany");
    }

    // Najpierw synchronizujemy profil w app_users
    const user = await getCurrentUser();

    console.log("Zalogowany użytkownik:", user.displayName);

    // Dopiero teraz renderujemy aplikację
    ReactDOM.createRoot(
      document.getElementById("root")!
    ).render(
      <React.StrictMode>
        <App />
      </React.StrictMode>
    );
  } catch (error) {
    console.error("Błąd inicjalizacji aplikacji:", error);

    ReactDOM.createRoot(
      document.getElementById("root")!
    ).render(
      <div className="flex min-h-screen items-center justify-center bg-slate-950 p-6 text-red-400">
        Nie udało się uruchomić aplikacji.
        Sprawdź konsolę przeglądarki.
      </div>
    );
  }
}

void bootstrap();
