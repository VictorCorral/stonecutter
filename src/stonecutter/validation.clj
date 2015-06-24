(ns stonecutter.validation
  (:require [clojure.string :as s]))

(def email-max-length 254)

(def password-min-length 8)

(def password-max-length 50)

(defn is-email-valid? [{email :email}]
  (when email
    (re-matches #"\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]+\b" email)))

(defn is-too-long? [string max-length]
  (> (count string) max-length))

(defn is-too-short? [string min-length]
  (< (count string) min-length))

(defn validate-email [is-duplicate-user-fn params]
  (cond (is-too-long? (:email params) email-max-length) :too-long
        (not (is-email-valid? params)) :invalid
        (is-duplicate-user-fn (:email params)) :duplicate
        :default nil))

(defn validate-password [params]
  (cond (s/blank? (:password params)) :invalid
        (is-too-long? (:password params) password-max-length) :invalid
        (is-too-short? (:password params) password-min-length) :invalid
        :default nil))

(defn do-passwords-match? [{:keys [password confirm-password]}]
  (= confirm-password password))

(defn validate-if-passwords-match [params]
  (cond (not (do-passwords-match? params)) :invalid
        :default nil))

(defn registration-validations [is-duplicate-user-fn]
  {:email            (partial validate-email is-duplicate-user-fn)
   :password         validate-password
   :confirm-password validate-if-passwords-match})

(defn run-validation [params [validation-key validation-fn]]
  [validation-key (validation-fn params)])

(defn validate-registration [params duplicate-user-fn]
  (->> (registration-validations duplicate-user-fn)
       (map (partial run-validation params))
       (remove (comp nil? second))
       (into {})))