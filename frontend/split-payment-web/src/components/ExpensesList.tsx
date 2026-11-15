
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  getGroupExpenses,
  type Expense,
} from "@/api/expensesApi";
import type { Group } from "@/api/groupsApi";
import { CreateExpenseForm } from "@/components/CreateExpenseForm";
import { Button } from "@/components/ui/button";

interface ExpensesListProps {
  group: Group;
}

export function ExpensesList({ group }: ExpensesListProps) {
  const { t, i18n } = useTranslation();

  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const locale = i18n.resolvedLanguage?.startsWith("pl")
    ? "pl-PL"
    : "en-US";

  useEffect(() => {
    let active = true;

    setLoading(true);
    setErrorKey(null);
    setExpenses([]);

    getGroupExpenses(group.id)
      .then((data) => {
        if (active) {
          setExpenses(data);
        }
      })
      .catch(() => {
        if (active) {
          setErrorKey("expenses.loadError");
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [group.id]);

  function handleExpenseCreated(expense: Expense) {
    setExpenses((current) => [expense, ...current]);
    setShowForm(false);
    setErrorKey(null);
  }

  function getUserName(userId: string): string {
    const member = group.members.find(
      (member) => member.userId === userId
    );

    return member?.displayName ?? t("common.unknownUser");
  }

  const totalAmount = expenses.reduce(
    (sum, expense) => sum + Math.round(expense.amount * 100),
    0
  ) / 100;

  const formatAmount = (amount: number) =>
    amount.toLocaleString(locale, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });

  return (
    <section className="space-y-5">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="text-xl font-semibold text-white">
          {t("expenses.title")}
        </h3>

        {!showForm && (
          <Button
            type="button"
            onClick={() => setShowForm(true)}
            className="bg-teal-400 text-slate-950 hover:bg-teal-300"
          >
            + {t("expenses.add")}
          </Button>
        )}
      </div>

      {/* Create expense form */}
      {showForm && (
        <CreateExpenseForm
          group={group}
          onCreated={handleExpenseCreated}
          onCancel={() => setShowForm(false)}
        />
      )}

      {/* Loading */}
      {loading && (
        <p className="text-slate-400">
          {t("expenses.loading")}
        </p>
      )}

      {/* Error */}
      {errorKey && (
        <p role="alert" className="text-red-400">
          {t(errorKey)}
        </p>
      )}

      {/* Summary and expense list */}
      {!loading && !errorKey && (
        <>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
              <p className="text-sm text-slate-400">
                {t("expenses.total")}
              </p>

              <p className="mt-2 text-2xl font-bold text-teal-300">
                {formatAmount(totalAmount)}
              </p>
            </div>

            <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
              <p className="text-sm text-slate-400">
                {t("expenses.count")}
              </p>

              <p className="mt-2 text-2xl font-bold text-white">
                {expenses.length}
              </p>
            </div>
          </div>

          {expenses.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-white/15 p-8 text-center">
              <p className="text-slate-400">
                {t("expenses.empty")}
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {expenses.map((expense) => (
                <div
                  key={expense.id}
                  className="rounded-xl border border-white/10 bg-white/5 p-4"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="min-w-0">
                      <h4 className="break-words font-semibold text-white">
                        {expense.description}
                      </h4>

                      <p className="mt-1 text-xs text-slate-400">
                        {t("expenses.paidBy")}{" "}
                        <span className="font-medium text-slate-200">
                          {getUserName(expense.paidBy)}
                        </span>
                      </p>

                      <p className="mt-1 text-xs text-slate-500">
                        {t("expenses.participantsCount")}:{" "}
                        {expense.splits.length}
                      </p>
                    </div>

                    <p className="shrink-0 font-semibold text-teal-300">
                      {formatAmount(expense.amount)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </section>
  );
}
