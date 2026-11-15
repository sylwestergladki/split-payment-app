
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { keycloak } from "@/auth/keycloak";
import { GroupsList } from "@/components/GroupsList";
import { GroupDetails } from "@/components/GroupDetails";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import { Button } from "@/components/ui/button";

function App() {
  const { t } = useTranslation();

  const [showGroups, setShowGroups] = useState(false);
  const [selectedGroupId, setSelectedGroupId] =
    useState<string | null>(null);

  const username =
    keycloak.tokenParsed?.preferred_username ??
    t("common.user");

  const handleLogout = () => {
    void keycloak.logout({
      redirectUri: window.location.origin,
    });
  };

  const handleToggleGroups = () => {
    setShowGroups((current) => !current);
    setSelectedGroupId(null);
  };

  return (
    <div className="relative min-h-screen overflow-hidden bg-slate-950 text-white">
      <div className="pointer-events-none absolute -left-32 top-20 h-80 w-80 rounded-full bg-teal-500/20 blur-3xl" />
      <div className="pointer-events-none absolute -right-32 bottom-10 h-96 w-96 rounded-full bg-violet-600/20 blur-3xl" />

      <main className="relative z-10 mx-auto flex min-h-screen w-full max-w-5xl flex-col justify-center px-4 py-10 sm:px-6">
        <div className="rounded-3xl border border-white/10 bg-slate-900/80 p-6 shadow-2xl shadow-black/30 backdrop-blur-xl sm:p-10">

          <div className="mb-10 flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-white/10 bg-white/5 px-4 py-3">
            <p className="text-sm text-slate-400">
              {t("app.loggedInAs")}:{" "}
              <span className="font-semibold text-teal-300">
                {username}
              </span>
            </p>

            <div className="flex items-center gap-3">
              <LanguageSwitcher />

              <Button
                variant="outline"
                size="sm"
                className="border-white/20 bg-white/5 text-white hover:bg-white/10 hover:text-white"
                onClick={handleLogout}
              >
                {t("app.logout")}
              </Button>
            </div>
          </div>

          <div className="flex flex-col items-center text-center">
            <div className="mb-8 inline-flex items-center gap-2 rounded-full border border-teal-400/20 bg-teal-400/10 px-4 py-2 text-sm font-medium text-teal-300">
              <span className="h-2 w-2 rounded-full bg-teal-400" />
              {t("app.name")}
            </div>

            <h1 className="max-w-2xl text-4xl font-bold tracking-tight sm:text-5xl">
              {t("app.title")}
              <span className="block bg-gradient-to-r from-teal-300 to-violet-400 bg-clip-text text-transparent">
                {t("app.subtitle")}
              </span>
            </h1>

            <p className="mt-6 max-w-xl text-base leading-7 text-slate-400">
              {t("app.description")}
            </p>

            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Button
                className="bg-teal-400 text-slate-950 hover:bg-teal-300"
                onClick={handleToggleGroups}
              >
                {showGroups
                  ? t("app.hideGroups")
                  : t("app.myGroups")}
              </Button>

              <Button
                variant="outline"
                className="border-white/20 bg-white/5 text-white hover:bg-white/10 hover:text-white"
                onClick={() => {
                  setShowGroups(true);
                  setSelectedGroupId(null);
                }}
              >
                {t("app.addExpense")}
              </Button>
            </div>
          </div>

          {showGroups && (
            <section className="mt-10 border-t border-white/10 pt-8">
              {selectedGroupId ? (
                <GroupDetails
                  groupId={selectedGroupId}
                  onBack={() => setSelectedGroupId(null)}
                />
              ) : (
                <>
                  <h2 className="mb-5 text-center text-xl font-semibold">
                    {t("groups.title")}
                  </h2>

                  <GroupsList
                    onSelectGroup={setSelectedGroupId}
                  />
                </>
              )}
            </section>
          )}

          <div className="mt-12 grid gap-4 border-t border-white/10 pt-8 sm:grid-cols-3">
            <div className="rounded-2xl bg-white/5 p-5 text-center">
              <div className="mb-2 text-2xl">👥</div>
              <h3 className="font-semibold">
                {t("features.groups.title")}
              </h3>
              <p className="mt-2 text-sm text-slate-400">
                {t("features.groups.description")}
              </p>
            </div>

            <div className="rounded-2xl bg-white/5 p-5 text-center">
              <div className="mb-2 text-2xl">💳</div>
              <h3 className="font-semibold">
                {t("features.expenses.title")}
              </h3>
              <p className="mt-2 text-sm text-slate-400">
                {t("features.expenses.description")}
              </p>
            </div>

            <div className="rounded-2xl bg-white/5 p-5 text-center">
              <div className="mb-2 text-2xl">⚖️</div>
              <h3 className="font-semibold">
                {t("features.balances.title")}
              </h3>
              <p className="mt-2 text-sm text-slate-400">
                {t("features.balances.description")}
              </p>
            </div>
          </div>
        </div>

        <p className="mt-6 text-center text-xs text-slate-500">
          {t("app.footer")}
        </p>
      </main>
    </div>
  );
}

export default App;
