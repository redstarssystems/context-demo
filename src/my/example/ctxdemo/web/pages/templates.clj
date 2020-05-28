(ns my.example.ctxdemo.web.pages.templates
  (:require [hiccup.core :as hiccup]
            [unifier.response :as r]))

(defn simple
  "simple template"
  [data]
  (hiccup/html
    [:html {:lang "en"}
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
      [:title "webapp01"]]
     [:body
      data]]))

(defn main
  "main template"
  [data]
  (hiccup/html
    [:html {:lang "en"}
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
      [:link {:rel "stylesheet" :href "/css/bootstrap.min.css" :type "text/css"}]
      [:link {:rel "stylesheet" :href "/css/app.css" :type "text/css"}]
      [:title "webapp01"]]
     [:body
      data
      [:script {:src "/js/jquery-3.4.1.slim.min.js" :type "text/javascript"}]
      [:script {:src "/js/bootstrap.bundle.min.js" :type "text/javascript"}]]]))

(defn not-found-handler
  "not found handler"
  [req]
  (r/as-not-found (simple [:div [:h2 "Page not found."]])))
