
import { apiClient } from "@/api/apiClient";

export interface CurrentUser {
  id: string;
  email: string;
  displayName: string;
  createdAt: string;
}

export function getCurrentUser(): Promise<CurrentUser> {
  return apiClient<CurrentUser>("/me");
}
