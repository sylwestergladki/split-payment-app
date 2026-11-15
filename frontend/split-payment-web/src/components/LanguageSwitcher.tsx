
import { useTranslation } from "react-i18next";

export function LanguageSwitcher() {
  const { i18n } = useTranslation();

  const currentLanguage =
    i18n.resolvedLanguage?.startsWith("pl") ? "pl" : "en";

  function changeLanguage(language: "pl" | "en") {
    localStorage.setItem("language", language);
    void i18n.changeLanguage(language);
  }

  return (
    <div
      role="group"
      aria-label="Language / Język"
      className="flex rounded-lg border border-white/15 bg-white/5 p-1"
    >
      {(["pl", "en"] as const).map((language) => (
        <button
          key={language}
          type="button"
          onClick={() => changeLanguage(language)}
          aria-pressed={currentLanguage === language}
          className={`rounded-md px-3 py-1.5 text-xs font-semibold transition-colors ${
            currentLanguage === language
              ? "bg-teal-400 text-slate-950"
              : "text-slate-400 hover:text-white"
          }`}
        >
          {language.toUpperCase()}
        </button>
      ))}
    </div>
  );
}
