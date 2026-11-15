
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getMyGroups, type Group } from "@/api/groupsApi";
import { CreateGroupForm } from "@/components/CreateGroupForm";
import { Button } from "@/components/ui/button";

interface GroupsListProps {
  onSelectGroup: (groupId: string) => void;
}

export function GroupsList({
  onSelectGroup,
}: GroupsListProps) {
  const { t, i18n } = useTranslation();

  const [groups, setGroups] = useState<Group[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const locale = i18n.resolvedLanguage?.startsWith("pl")
    ? "pl-PL"
    : "en-US";

  function formatDate(date: string): string {
    return new Date(date).toLocaleDateString(locale, {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }

  useEffect(() => {
    let active = true;

    setLoading(true);
    setErrorKey(null);

    getMyGroups()
      .then((data) => {
        if (active) {
          setGroups(data);
        }
      })
      .catch(() => {
        if (active) {
          setErrorKey("groups.loadError");
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
  }, []);

  function handleGroupCreated(group: Group) {
    setGroups((current) => [group, ...current]);
    setShowForm(false);
    setErrorKey(null);
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-400">
          {t("groups.description")}
        </p>

        {!showForm && (
          <Button
            type="button"
            onClick={() => setShowForm(true)}
            className="bg-teal-400 text-slate-950 hover:bg-teal-300"
          >
            + {t("groups.new")}
          </Button>
        )}
      </div>

      {/* Create group form */}
      {showForm && (
        <CreateGroupForm
          onCreated={handleGroupCreated}
          onCancel={() => setShowForm(false)}
        />
      )}

      {/* Loading */}
      {loading && (
        <p className="text-slate-400">
          {t("groups.loading")}
        </p>
      )}

      {/* Error */}
      {errorKey && (
        <p role="alert" className="text-red-400">
          {t(errorKey)}
        </p>
      )}

      {/* Empty state */}
      {!loading && !errorKey && groups.length === 0 && (
        <div className="rounded-2xl border border-dashed border-white/15 p-8 text-center">
          <p className="font-medium text-white">
            {t("groups.empty")}
          </p>

          <p className="mt-2 text-sm text-slate-400">
            {t("groups.emptyHint")}
          </p>
        </div>
      )}

      {/* Groups */}
      {!loading && !errorKey && groups.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2">
          {groups.map((group) => (
            <button
              key={group.id}
              type="button"
              onClick={() => onSelectGroup(group.id)}
              className="w-full cursor-pointer rounded-2xl border border-white/10 bg-white/5 p-5 text-left transition-colors hover:border-teal-400/40 hover:bg-white/10 focus-visible:outline-2 focus-visible:outline-teal-400"
            >
              <div className="mb-3 flex items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-teal-400/10 text-xl">
                  👥
                </div>

                <h3 className="min-w-0 break-words font-semibold text-white">
                  {group.name}
                </h3>
              </div>

              <p className="text-sm text-slate-400">
                {t("groups.members")}:{" "}
                {group.members?.length ?? 0}
              </p>

              <p className="mt-1 text-xs text-slate-500">
                {t("groups.createdAt")}:{" "}
                {formatDate(group.createdAt)}
              </p>

              <p className="mt-4 text-sm font-medium text-teal-300">
                {t("groups.details")} →
              </p>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
