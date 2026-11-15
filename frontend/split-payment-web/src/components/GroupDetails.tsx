
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  getGroupDetails,
  type Group,
} from "@/api/groupsApi";
import { Button } from "@/components/ui/button";
import { ExpensesList } from "@/components/ExpensesList";

interface GroupDetailsProps {
  groupId: string;
  onBack: () => void;
}

export function GroupDetails({
  groupId,
  onBack,
}: GroupDetailsProps) {
  const { t, i18n } = useTranslation();

  const [group, setGroup] = useState<Group | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorKey, setErrorKey] = useState<string | null>(null);

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
    setGroup(null);

    getGroupDetails(groupId)
      .then((data) => {
        if (active) {
          setGroup(data);
        }
      })
      .catch(() => {
        if (active) {
          setErrorKey("groups.detailsError");
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
  }, [groupId]);

  return (
    <div className="space-y-6">
      {/* Back button */}
      <Button
        type="button"
        variant="outline"
        onClick={onBack}
        className="border-white/20 bg-white/5 text-white hover:bg-white/10 hover:text-white"
      >
        ← {t("groups.back")}
      </Button>

      {/* Loading */}
      {loading && (
        <p className="text-slate-400">
          {t("groups.loadingDetails")}
        </p>
      )}

      {/* Error */}
      {errorKey && (
        <p role="alert" className="text-red-400">
          {t(errorKey)}
        </p>
      )}

      {/* Group details */}
      {!loading && !errorKey && group && (
        <>
          {/* Group header */}
          <div className="rounded-2xl border border-teal-400/20 bg-teal-400/5 p-6">
            <p className="mb-2 text-sm text-teal-300">
              {t("groups.detailsTitle")}
            </p>

            <h2 className="text-3xl font-bold text-white">
              {group.name}
            </h2>

            <p className="mt-3 text-sm text-slate-400">
              {t("groups.createdAt")}:{" "}
              {formatDate(group.createdAt)}
            </p>
          </div>

          {/* Group statistics */}
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
              <p className="text-sm text-slate-400">
                {t("groups.memberCount")}
              </p>

              <p className="mt-2 text-3xl font-bold text-teal-300">
                {group.members.length}
              </p>
            </div>

            <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
              <p className="text-sm text-slate-400">
                {t("groups.groupId")}
              </p>

              <p className="mt-2 break-all text-xs text-slate-300">
                {group.id}
              </p>
            </div>
          </div>

          {/* Group members */}
          <section className="space-y-4">
            <h3 className="text-xl font-semibold text-white">
              {t("groups.membersTitle")}
            </h3>

            {group.members.length === 0 ? (
              <p className="text-slate-400">
                {t("groups.noMembers")}
              </p>
            ) : (
              <div className="space-y-3">
                {group.members.map((member) => (
                  <div
                    key={member.userId}
                    className="flex items-center gap-4 rounded-xl border border-white/10 bg-white/5 p-4"
                  >
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-violet-400/10 text-violet-300">
                      👤
                    </div>

                    <div className="min-w-0">
                      <p className="break-words text-sm font-medium text-white">
                        {member.displayName ||
                          t("common.unknownUser")}
                      </p>

                      <p className="mt-1 text-xs text-slate-400">
                        {t("groups.joinedAt")}:{" "}
                        {formatDate(member.joinedAt)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Expenses */}
          <ExpensesList group={group} />
        </>
      )}
    </div>
  );
}
