
import { apiClient } from "@/api/apiClient";

export interface ExpenseSplit {
  userId: string;
  amount: number;
}

export interface Expense {
  id: string;
  groupId: string;
  description: string;
  amount: number;
  paidBy: string;
  splits: ExpenseSplit[];
  createdAt: string;
}

export interface CreateExpenseRequest {
  description: string;
  amount: number;
  paidBy: string;
  participants: string[];
}

export function getGroupExpenses(
  groupId: string
): Promise<Expense[]> {
  return apiClient<Expense[]>(
    `/groups/${groupId}/expenses`
  );
}

export function createExpense(
  groupId: string,
  request: CreateExpenseRequest
): Promise<Expense> {
  return apiClient<Expense>(
    `/groups/${groupId}/expenses`,
    {
      method: "POST",
      body: JSON.stringify(request),
    }
  );
}
