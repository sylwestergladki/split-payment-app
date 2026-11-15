
import { useState, type FormEvent } from "react";
import { useTranslation } from "react-i18next";
import { createGroup, type Group } from "@/api/groupsApi";
import { Button } from "@/components/ui/button";

interface CreateGroupFormProps {
  onCreated: (group: Group) => void;
  onCancel: () => void;
}

export function CreateGroupForm({
  onCreated,
  onCancel,
}: CreateGroupFormProps) {
  const { t } = useTranslation();

  const [name, setName] = useState("");
  const [saving, setSaving] = useState(false);
  const [errorKey, setErrorKey] = useState<string | null>(null);

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    const trimmedName = name.trim();

    if (!trimmedName) {
      setErrorKey("groups.nameRequired");
      return;
    }

    setSaving(true);
    setErrorKey(null);

    try {
      const group = await createGroup({
        name: trimmedName,
      });

      setName("");
      onCreated(group);
    } catch {
      setErrorKey("groups.createError");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-4 rounded-2xl border border-teal-400/20 bg-white/5 p-5"
    >
      {/* Header */}
      <div>
        <h3 className="text-lg font-semibold text-white">
          {t("groups.new")}
        </h3>

        <p className="mt-1 text-sm text-slate-400">
          {t("groups.formDescription")}
        </p>
      </div>

      {/* Group name */}
      <div className="space-y-2">
        <label
          htmlFor="group-name"
          className="block text-sm font-medium text-slate-300"
        >
          {t("groups.name")}
        </label>

        <input
          id="group-name"
          type="text"
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder={t("groups.namePlaceholder")}
          disabled={saving}
          required
          autoFocus
          className="w-full rounded-xl border border-white/15 bg-slate-950/70 px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-teal-400"
        />
      </div>

      {/* Error message */}
      {errorKey && (
        <p
          role="alert"
          className="text-sm text-red-400"
        >
          {t(errorKey)}
        </p>
      )}

      {/* Actions */}
      <div className="flex flex-wrap gap-3">
        <Button
          type="submit"
          disabled={saving}
          className="bg-teal-400 text-slate-950 hover:bg-teal-300"
        >
          {saving
            ? t("groups.creating")
            : t("groups.create")}
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
