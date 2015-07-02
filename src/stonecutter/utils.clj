(ns stonecutter.utils
  (:require [ring.util.response :as r]))

(defn html-response [s]
  (-> s
      r/response
      (r/content-type "text/html")))