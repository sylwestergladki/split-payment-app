
import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import en from "./en.json";
import pl from "./pl.json";

const savedLanguage = localStorage.getItem("language");

const initialLanguage =
  savedLanguage === "pl" || savedLanguage === "en"
    ? savedLanguage
    : "en";

void i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    pl: { translation: pl },
  },
  lng: initialLanguage,
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;
