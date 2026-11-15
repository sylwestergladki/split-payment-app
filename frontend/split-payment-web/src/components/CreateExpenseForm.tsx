
import { useState, type FormEvent } from "react";
import { useTranslation } from "react-i18next";
import { createExpense, type Expense } from "@/api/expensesApi";
import type { Group } from "@/api/groupsApi";
import { Button } from "@/components/ui/button";

interface CreateExpenseFormProps {
  group: Group;
  onCreated: (expense: Expense) => void;
  onCancel: () => void;
}

export function CreateExpenseForm({
  group,
  onCreated,
  onCancel,
}: CreateExpenseFormProps) {
  const { t, i18n } = useTranslation();

  const members = group.members;

  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [paidBy, setPaidBy] = useState(
    members[0]?.userId ?? ""
  );
  const [participants, setParticipants] = useState<string[]>(
    members.map((member) => member.userId)
  );
  const [saving, setSaving] = useState(false);
  const [errorKey, setErrorKey] = useState<string | null>(null);

  const isPolish = i18n.resolvedLanguage?.startsWith("pl");

  function toggleParticipant(userId: string) {
    setParticipants((current) =>
      current.includes(userId)
        ? current.filter((id) => id !== userId)
        : [...current, userId]
    );
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    const trimmedDescription = description.trim();
    const normalizedAmount = amount.trim().replace(",", ".");
    const parsedAmount = Number(normalizedAmount);

    if (!trimmedDescription) {
      setErrorKey("expenses.descriptionRequired");
      return;
    }

    if (
      !normalizedAmount ||
      !/^\d+(\.\d{1,2})?$/.test(normalizedAmount) ||
      !Number.isFinite(parsedAmount) ||
      parsedAmount <= 0
    ) {
      setErrorKey("expenses.amountInvalid");
      return;
    }

    if (!paidBy || !members.some((member) => member.userId === paidBy)) {
      setErrorKey("expenses.payerRequired");
      return;
    }

    if (
      participants.length === 0 ||
      participants.some(
        (userId) => !members.some((member) => member.userId === userId)
      )
    ) {
      setErrorKey("expenses.participantsRequired");
      return;
    }

    setSaving(true);
    setErrorKey(null);

    try {
      const expense = await createExpense(group.id, {
        description: trimmedDescription,
        amount: parsedAmount,
        paidBy,
        participants,
      });

      onCreated(expense);
    } catch {
      setErrorKey("expenses.createError");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-5 rounded-2xl border border-teal-400/20 bg-white/5 p-5"
    >
      <h3 className="text-lg font-semibold text-white">
        {t("expenses.new")}
      </h3>

      {/* Description */}
      <div className="space-y-2">
        <label
          htmlFor="expense-description"
          className="block text-sm text-slate-300"
        >
          {t("expenses.description")}
        </label>

        <input
          id="expense-description"
          type="text"
          value={description}
          onChange={(event) =>
            setDescription(event.target.value)
          }
          placeholder={t("expenses.descriptionPlaceholder")}
          required
          disabled={saving}
          className="w-full rounded-xl border border-white/15 bg-slate-950 p-3 text-white outline-none placeholder:text-slate-500 focus:border-teal-400"
        />
      </div>

      {/* Amount */}
      <div className="space-y-2">
        <label
          htmlFor="expense-amount"
          className="block text-sm text-slate-300"
        >
          {t("expenses.amount")}
        </label>

        <input
          id="expense-amount"
          type="text"
          inputMode="decimal"
          value={amount}
          onChange={(event) =>
            setAmount(event.target.value)
          }
          placeholder={isPolish ? "150,00" : "150.00"}
          required
          disabled={saving}
          className="w-full rounded-xl border border-white/15 bg-slate-950 p-3 text-white outline-none placeholder:text-slate-500 focus:border-teal-400"
        />
      </div>

      {/* Payer */}
      <div className="space-y-2">
        <label
          htmlFor="expense-paid-by"
          className="block text-sm text-slate-300"
        >
          {t("expenses.paidBy")}
        </label>

        <select
          id="expense-paid-by"
          value={paidBy}
          onChange={(event) =>
            setPaidBy(event.target.value)
          }
          disabled={saving || members.length === 0}
          className="w-full rounded-xl border border-white/15 bg-slate-950 p-3 text-white outline-none focus:border-teal-400"
        >
          {members.map((member) => (
            <option
              key={member.userId}
              value={member.userId}
            >
              {member.displayName}
            </option>
          ))}
        </select>
      </div>

      {/* Participants */}
      <fieldset className="space-y-3">
        <legend className="mb-3 text-sm text-slate-300">
          {t("expenses.participants")}
        </legend>

        {members.map((member) => (
          <label
            key={member.userId}
            className="flex cursor-pointer items-center gap-3 rounded-xl border border-white/10 p-3"
          >
            <input
              type="checkbox"
              checked={participants.includes(member.userId)}
              onChange={() =>
                toggleParticipant(member.userId)
              }
              disabled={saving}
              className="accent-teal-400"
            />

            <span className="text-sm text-slate-300">
              {member.displayName}
            </span>
          </label>
        ))}
      </fieldset>

      {/* Validation errors */}
      {errorKey && (
        <p role="alert" className="text-sm text-red-400">
          {t(errorKey)}
        </p>
      )}

      {/* Actions */}
      <div className="flex flex-wrap gap-3">
        <Button
          type="submit"
          disabled={saving || members.length === 0}
          className="bg-teal-400 text-slate-950 hover:bg-teal-300"
        >
          {saving
            ? t("expenses.saving")
            : t("expenses.add")}
        </Button>

        <Button
          type="button"
          variant="outline"
          disabled={saving}
          onClick={onCancel}
          className="border-white/20 bg-white/5 text-white hover:bg-white/10 hover:text-white"
        >
          {t("common.cancel")}
        </Button>
      </div>
    </form>
  );
}
