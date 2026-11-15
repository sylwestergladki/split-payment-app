
import { keycloak } from "@/auth/keycloak";

export async function apiClient<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  if (!keycloak.authenticated) {
    throw new Error("Użytkownik nie jest zalogowany");
  }

  await keycloak.updateToken(30);

  const headers = new Headers(options.headers);

  if (!keycloak.token) {
    throw new Error("Brak tokenu dostępu");
  }

  headers.set("Authorization", `Bearer ${keycloak.token}`);

  if (options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`/api${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new Error(`Błąd API: ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
